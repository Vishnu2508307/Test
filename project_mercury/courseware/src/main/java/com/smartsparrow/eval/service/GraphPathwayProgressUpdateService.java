package com.smartsparrow.eval.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerGraphPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.UUIDs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

@Singleton
public class GraphPathwayProgressUpdateService implements PathwayProgressUpdateService<LearnerGraphPathway> {

    private final ProgressService progressService;
    private final AttemptService attemptService;
    private final LearnerPathwayService learnerPathwayService;
    private final StudentScopeService studentScopeService;

    @Inject
    public GraphPathwayProgressUpdateService(final ProgressService progressService,
                                             final AttemptService attemptService,
                                             final LearnerPathwayService learnerPathwayService,
                                             final StudentScopeService studentScopeService) {
        this.progressService = progressService;
        this.attemptService = attemptService;
        this.learnerPathwayService = learnerPathwayService;
        this.studentScopeService = studentScopeService;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(LearnerGraphPathway pathway, ProgressAction action, LearnerEvaluationResponseContext context) {
        // prepare variables
        final LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();

        final Progress childProgress = context.getProgresses().get(context.getProgresses().size() - 1);
        final UUID pathwayId = pathway.getId();
        final UUID deploymentId = pathway.getDeploymentId();
        final UUID studentId = request.getStudentId();

        // find the child attempt
        Mono<Attempt> childAttemptMono = attemptService.findById(childProgress.getAttemptId());

        // find the previous progress
        Mono<GraphPathwayProgress> previousProgressMono = progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId)
                // if there is no previous progress, create a new one using the configured starting walkable
                .switchIfEmpty(Mono.defer(() -> pathway.getConfiguredWalkable()
                        .singleOrEmpty()
                        .map(startingWalkable -> new GraphPathwayProgress()
                                .setCurrentWalkableId(startingWalkable.getElementId())
                                .setCurrentWalkableType(startingWalkable.getElementType()))));
        // Get the currently configured walkables.
        Mono<List<WalkableChild>> walkableChildrenMono = learnerPathwayService.findWalkables(pathwayId, deploymentId) //
                .collectList();

        return Mono.zip(childAttemptMono, previousProgressMono, walkableChildrenMono)
                .flatMap(tuple3 -> {
                    final Attempt childAttempt = tuple3.getT1();
                    final GraphPathwayProgress previousProgress = tuple3.getT2();
                    // get this pathway attempt id
                    final UUID attemptId = childAttempt.getParentId();
                    List<WalkableChild> walkableChildren = tuple3.getT3();

                    // build the progress for this graph pathway
                    return buildGraphPathwayProgress(context, childProgress, attemptId, previousProgress, walkableChildren, pathwayId)
                            .flatMap(progress -> progressService.persist(progress)
                                    .then(Mono.just(progress)));
                });
    }

    /**
     * Build the graph pathway progress. When the evaluatedElementId is a direct child of the pathway then the progress
     * is built considering the progression type defined in the action. When not then compute the completion value and
     * build the progress.
     *
     * @param context          the learner evaluation context information
     * @param childProgress    the child element progress previously generated
     * @param attemptId        the pathway attempt id
     * @param previousProgress the previous graph pathway progress
     * @param pathwayId        the pathway to build the progress for
     * @return a new graph pathway progress
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    private Mono<GraphPathwayProgress> buildGraphPathwayProgress(final LearnerEvaluationResponseContext context,
                                                                 final Progress childProgress,
                                                                 final UUID attemptId,
                                                                 final GraphPathwayProgress previousProgress,
                                                                 final List<WalkableChild> walkableChildren,
                                                                 final UUID pathwayId) {

        final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
        final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

        // Perform the completion calculation
        Map<UUID, Float> newCompletionValues = merge(walkableChildren, prevComplValues,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getValue());
        Map<UUID, Float> newCompletionConfidence = merge(walkableChildren, prevComplConfidence,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getConfidence());


        // if I am the direct parent of the evaluated element I want to know about the action progression type
        if (context.getEvaluationActionState().getCoursewareElement().getElementId().equals(childProgress.getCoursewareElementId())) {
            return buildProgressFromAction(context, attemptId, previousProgress, walkableChildren, newCompletionValues, newCompletionConfidence, pathwayId);
        }
        // for any other case, just build a normal progress
        return Mono.just(buildNormalProgress(context, attemptId, previousProgress, walkableChildren, newCompletionValues, newCompletionConfidence, pathwayId));
    }

    /**
     * Calculate the completion value and build the progress
     *
     * @param context           holds the learner evaluation context information
     * @param attemptId         the pathway attempt
     * @param previousProgress  the previous graph pathway progress
     * @param childrenWalkables the current pathway walkable children
     * @param pathwayId         the pathway to build the progress for
     * @return a new graph pathway progress
     */
    private GraphPathwayProgress buildNormalProgress(final LearnerEvaluationResponseContext context,
                                                     final UUID attemptId,
                                                     final GraphPathwayProgress previousProgress,
                                                     final List<WalkableChild> childrenWalkables,
                                                     final Map<UUID, Float> newCompletionValues,
                                                     final Map<UUID, Float> newCompletionConfidence,
                                                     final UUID pathwayId) {

        // calculate the completion
        Completion completion = calculateCompletion(newCompletionValues, newCompletionConfidence, childrenWalkables);
        // build the progress with the previous current walkable
        WalkableChild currentWalkabe = new WalkableChild()
                .setElementId(previousProgress.getCurrentWalkableId())
                .setElementType(previousProgress.getCurrentWalkableType());
        return buildProgress(context, attemptId, completion, currentWalkabe, newCompletionValues, newCompletionConfidence, pathwayId);
    }

    /**
     * Build a new graph pathway progress based on the progression type defined in the action context.
     * <ul>
     *     <li>{@link ProgressionType#INTERACTIVE_COMPLETE_AND_GO_TO} - calculate the completion value and build a
     *     progress where the current walkable is defined by the action context</li>
     *     <li>{@link ProgressionType#INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE} - Build a completed progress keeping the previous walkable as
     *     the pathway current walkable</li>
     * </ul>
     *
     * @param context                 holds the evaluation context information
     * @param attemptId               the pathway attempt id
     * @param previousProgress        the previous graph pathway progress
     * @param childrenWalkables       the current pathway walkable children
     * @param newCompletionValues     map of new completion values
     * @param newCompletionConfidence map of new completion confidence
     * @param pathwayId               the pathway id to build the progress for
     * @return a new graph patwhay progress
     * @throws IllegalArgumentFault when the new current walkable defined by
     *                              {@link ProgressionType#INTERACTIVE_COMPLETE_AND_GO_TO} is not a child of the current pathway
     */
    private Mono<GraphPathwayProgress> buildProgressFromAction(final LearnerEvaluationResponseContext context,
                                                               final UUID attemptId,
                                                               final GraphPathwayProgress previousProgress,
                                                               final List<WalkableChild> childrenWalkables,
                                                               final Map<UUID, Float> newCompletionValues,
                                                               final Map<UUID, Float> newCompletionConfidence,
                                                               final UUID pathwayId) {
        // get the progress action context and the progression type
        final EvaluationActionState actionState = context.getEvaluationActionState();
        final ProgressActionContext actionContext = actionState.getProgressActionContext();
        final ProgressionType progressionType = actionContext.getProgressionType();
        final LearnerEvaluationRequest request = context.getResponse().getEvaluationRequest();
        final Deployment deployment = request.getDeployment();
        final UUID studentId = request.getStudentId();

        switch (progressionType) {
            case INTERACTIVE_COMPLETE_AND_GO_TO:
            case ACTIVITY_COMPLETE_AND_GO_TO:
                // calculate the completion normally
                Completion completion = calculateCompletion(newCompletionValues, newCompletionConfidence, childrenWalkables);

                // check that the new currentWalkable is a child of the current pathway
                boolean targetIsAChild = childrenWalkables.stream()
                        .anyMatch(child -> child.getElementId().equals(actionContext.getElementId()));
                // throw a fault when the currentWalkable is not a child of the current pathway
                affirmArgument(targetIsAChild, "cannot GO_TO a walkable outside the current pathway");

                // Build a new attempt so that if we circle back we are not returning the previous attempt scope.
                return attemptService.findLatestAttempt(deployment.getId(), actionContext.getElementId(), request.getStudentId())
                        .onErrorResume(AttemptNotFoundFault.class, ex -> Mono.empty())
                        .flatMap(attempt -> attemptService.newAttempt(attempt.getDeploymentId(),
                                attempt.getStudentId(),
                                attempt.getCoursewareElementType(),
                                attempt.getCoursewareElementId(),
                                attemptId,
                                attempt.getValue() + 1))
                        .thenMany(studentScopeService.resetScopesFor(deployment.getId(), actionContext.getElementId(), studentId))
                        // set the next current walkable based on the action context data
                        .then(Mono.just(buildProgress(context, attemptId, completion, new WalkableChild()
                                        .setElementId(actionContext.getElementId())
                                        .setElementType(actionContext.getElementType()),
                                newCompletionValues, newCompletionConfidence, pathwayId)));
            case INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE:
            case ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE:
                // set the completion to 1 and build a graph pathway progress with the same current walkable
                // as the previous progress
                return Mono.just(buildProgress(context, attemptId, new Completion()
                                .setValue(1f)
                                .setConfidence(1f),
                        new WalkableChild()
                                .setElementId(previousProgress.getCurrentWalkableId())
                                .setElementType(previousProgress.getCurrentWalkableType()),
                        newCompletionValues,
                        newCompletionConfidence,
                        pathwayId));
            default:
                // for any other action just build a normal progress
                return Mono.just(buildNormalProgress(context, attemptId, previousProgress, childrenWalkables,
                        newCompletionValues, newCompletionConfidence, pathwayId));
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    private Completion calculateCompletion(final Map<UUID, Float> newCompletionValues,
                                           final Map<UUID, Float> newCompletionConfidence,
                                           final List<WalkableChild> childrenWalkables) {
        return buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                childrenWalkables.size());
    }

    private GraphPathwayProgress buildProgress(@Nonnull final LearnerEvaluationResponseContext context,
                                               @Nonnull final UUID attemptId,
                                               @Nonnull final Completion completion,
                                               @Nonnull final WalkableChild currentWalkable,
                                               @Nonnull final Map<UUID, Float> completionValues,
                                               @Nonnull final Map<UUID, Float> completionConfidence,
                                               @Nonnull final UUID pathwayId) {

        final LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();
        return new GraphPathwayProgress()
                .setId(UUIDs.timeBased())
                .setDeploymentId(request.getDeployment().getId())
                .setChangeId(request.getDeployment().getChangeId())
                .setCoursewareElementId(pathwayId)
                .setStudentId(request.getStudentId())
                .setAttemptId(attemptId)
                .setChildWalkableCompletionConfidences(completionValues)
                .setChildWalkableCompletionValues(completionConfidence)
                .setEvaluationId(context.getResponse().getWalkableEvaluationResult().getId())
                .setCompletion(completion)
                .setCurrentWalkableId(currentWalkable.getElementId())
                .setCurrentWalkableType(currentWalkable.getElementType());
    }

    @SuppressWarnings("Duplicates")
    Completion buildCompletionData(final Collection<Float> completionValues,
                                   final Collection<Float> completionConfidences,
                                   int totalSize) {
        // sum up the current completion values.
        double pSum = completionValues.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // sum up the current confidence values
        double cSum = completionConfidences.stream().filter(Objects::nonNull).mapToDouble(Float::floatValue).sum();
        // calcuate the values
        float progressValue = (float) (pSum / totalSize);
        float confidenceValue = (float) (cSum / totalSize);

        // sanity check. The only way to exit a graph pathway is via an action.
        // its completion can never go to 100% automatically
        progressValue = Math.min(progressValue, 0.95f);
        confidenceValue = Math.min(confidenceValue, 0.95f);

        // build the completion
        return new Completion()
                .setValue(progressValue)
                .setConfidence(confidenceValue);
    }

    @SuppressWarnings("Duplicates")
    Map<UUID, Float> merge(final List<WalkableChild> walkableChildren,
                           final Map<UUID, Float> prevValues,
                           final UUID childElementId,
                           final Float childValue) {

        Map<UUID, Float> ret = walkableChildren.stream()
                .filter(walkableChild -> prevValues.containsKey(walkableChild.getElementId()))
                .filter(walkableChild -> prevValues.get(walkableChild.getElementId()) != null)
                .collect(Collectors.toMap(WalkableChild::getElementId, walkableChild -> prevValues.get(walkableChild.getElementId())));

        if (childValue != null) {
            ret.put(childElementId, childValue);
        }

        return ret;
    }
}
