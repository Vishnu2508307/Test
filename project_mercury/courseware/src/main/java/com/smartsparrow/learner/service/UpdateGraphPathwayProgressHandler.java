package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerGraphPathway;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.UUIDs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

@Singleton
public class UpdateGraphPathwayProgressHandler extends UpdateProgressHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateGraphPathwayProgressHandler.class);

    private final ProgressService progressService;
    private final AttemptService attemptService;
    private final LearnerPathwayService learnerPathwayService;
    private final StudentScopeService studentScopeService;

    @Inject
    public UpdateGraphPathwayProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer,
                                             ProgressService progressService,
                                             AttemptService attemptService,
                                             LearnerPathwayService learnerPathwayService,
                                             StudentScopeService studentScopeService) {
        super(studentProgressRTMProducer);
        this.progressService = progressService;
        this.attemptService = attemptService;
        this.learnerPathwayService = learnerPathwayService;
        this.studentScopeService = studentScopeService;
    }

    @Handler
    public void updateProgress(final Exchange exchange) {

        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);
        // if this is an activity restart then the EvaluationEventMessage will be null
        final EvaluationEventMessage evaluationEventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        final Progress childProgress = event.getEventProgress().get(event.getEventProgress().size() - 1);
        final UUID pathwayId = event.getElement().getElementId();
        final UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();
        final UUID studentId = event.getUpdateProgressEvent().getStudentId();

        if (log.isDebugEnabled()) {
            log.debug("updating progress for graph pathway: {}", event.toString());
        }

        // find and build the current graph pathway
        LearnerGraphPathway pathway = learnerPathwayService.find(pathwayId, deploymentId, LearnerGraphPathway.class)
                .block();

        affirmNotNull(pathway, new IllegalStateFault("something is wrong. The pathway must exists"));

        // find the configured starting walkable
        final WalkableChild startingWalkable = pathway.getConfiguredWalkable()
                .blockFirst();

        affirmNotNull(startingWalkable, new IllegalStateFault("something is wrong. A configured starting walkable must be defined"));

        // find the child attempt
        Attempt childAttempt = attemptService.findById(childProgress.getAttemptId())
                .block();

        affirmNotNull(childAttempt, new IllegalStateFault("something is wrong. The attempt should exists"));

        // get this pathway attempt id
        final UUID attemptId = childAttempt.getParentId();

        // Load the previous progress; create if it doesn't exist as a placeholder.
        GraphPathwayProgress previousProgress = progressService.findLatestGraphPathway(deploymentId, pathwayId, studentId)
                .block();

        if(previousProgress == null) {
            previousProgress = new GraphPathwayProgress()
                    // set the current walkable for the previous progress from the pathway configuration
                    .setCurrentWalkableId(startingWalkable.getElementId())
                    .setCurrentWalkableType(startingWalkable.getElementType());
        }

        // build the progress for this graph pathway
        final GraphPathwayProgress progress = buildGraphPathwayProgress(event, childProgress, attemptId,
                evaluationEventMessage, previousProgress);

        // persist the graph pathway progress
        progressService.persist(progress).blockLast();

        // Broadcast the progress change
        broadcastProgressEventMessage(progress, event.getUpdateProgressEvent());
        // Roll the progress up the courseware structure
        propagateProgressChangeUpwards(exchange, event, progress);

    }

    /**
     * Build the graph pathway progress. When the evaluatedElementId is a direct child of the pathway then the progress
     * is built considering the progression type defined in the action. When not then compute the completion value and
     * build the progress.
     *
     * @param event the courseware element progress event
     * @param childProgress the child element progress previously generated
     * @param attemptId the pathway attempt id
     * @param evaluationEventMessage the evaluation event message
     * @param previousProgress the previous graph pathway progress
     * @return a new graph pathway progress
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    private GraphPathwayProgress buildGraphPathwayProgress(final UpdateCoursewareElementProgressEvent event,
                                                           final Progress childProgress,
                                                           final UUID attemptId,
                                                           final EvaluationEventMessage evaluationEventMessage,
                                                           final GraphPathwayProgress previousProgress) {
        // Get the currently configured walkables.
        List<WalkableChild> childrenWalkables = learnerPathwayService.findWalkables(event.getElement().getElementId(),
                event.getUpdateProgressEvent().getDeploymentId()) //
                .collectList() //
                .block();

        final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
        final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

        // Perform the completion calculation
        Map<UUID, Float> newCompletionValues = merge(childrenWalkables, prevComplValues,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getValue());
        Map<UUID, Float> newCompletionConfidence = merge(childrenWalkables, prevComplConfidence,
                childProgress.getCoursewareElementId(),
                childProgress.getCompletion().getConfidence());


        // if I am the direct parent of the evaluated element I want to know about the action progression type
        if (evaluationEventMessage != null && evaluationEventMessage.getEvaluationActionState().getCoursewareElement().getElementId().equals(childProgress.getCoursewareElementId())) {
            return buildProgressFromAction(event, evaluationEventMessage, attemptId, previousProgress, childrenWalkables,
                    newCompletionValues, newCompletionConfidence);
        }
        // for any other case, just build a normal progress
        return buildNormalProgress(event, attemptId, previousProgress, childrenWalkables,
                newCompletionValues, newCompletionConfidence);
    }

    /**
     * Calculate the completion value and build the progress
     *
     * @param event the courseware element progress event
     * @param attemptId the pathway attempt
     * @param previousProgress the previous graph pathway progress
     * @param childrenWalkables the current pathway walkable children
     * @return a new graph pathway progress
     */
    private GraphPathwayProgress buildNormalProgress(final UpdateCoursewareElementProgressEvent event,
                                                     final UUID attemptId,
                                                     final GraphPathwayProgress previousProgress,
                                                     final List<WalkableChild> childrenWalkables,
                                                     final Map<UUID, Float> newCompletionValues,
                                                     final Map<UUID, Float> newCompletionConfidence) {

        // calculate the completion
        Completion completion = calculateCompletion(newCompletionValues, newCompletionConfidence, childrenWalkables);
        // build the progress with the previous current walkable
        WalkableChild currentWalkabe = new WalkableChild()
                .setElementId(previousProgress.getCurrentWalkableId())
                .setElementType(previousProgress.getCurrentWalkableType());
        return buildProgress(event, attemptId, completion, currentWalkabe, newCompletionValues, newCompletionConfidence);
    }

    /**
     * Build a new graph pathway progress based on the progression type defined in the action context.
     * <ul>
     *     <li>{@link ProgressionType#INTERACTIVE_COMPLETE_AND_GO_TO} - calculate the completion value and build a
     *     progress where the current walkable is defined by the action context</li>
     *     <li>{@link ProgressionType#INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE} - Build a completed progress keeping the previous walkable as
     *     the pathway current walkable</li>
     * </ul>
     * @param event the courseware element progress event
     * @param attemptId the pathway attempt id
     * @param previousProgress the previous graph pathway progress
     * @param childrenWalkables the current pathway walkable children
     * @param newCompletionValues map of new completion values
     * @param newCompletionConfidence map of new completion confidence
     * @return a new graph patwhay progress
     * @throws IllegalArgumentFault when the new current walkable defined by
     * {@link ProgressionType#INTERACTIVE_COMPLETE_AND_GO_TO} is not a child of the current pathway
     */
    private GraphPathwayProgress buildProgressFromAction(final UpdateCoursewareElementProgressEvent event,
                                                         final EvaluationEventMessage evaluationEventMessage,
                                                         final UUID attemptId,
                                                         final GraphPathwayProgress previousProgress,
                                                         final List<WalkableChild> childrenWalkables,
                                                         final Map<UUID, Float> newCompletionValues,
                                                         final Map<UUID, Float> newCompletionConfidence) {
        // get the progress action context and the progression type
        final ProgressActionContext actionContext = evaluationEventMessage.getEvaluationActionState().getProgressActionContext();
        ProgressionType progressionType = actionContext.getProgressionType();

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
                Attempt lastAttempt = attemptService.findLatestAttempt(event.getUpdateProgressEvent().getDeploymentId(),
                                                 evaluationEventMessage.getEvaluationActionState().getProgressActionContext().getElementId(),
                                                 event.getUpdateProgressEvent().getStudentId())
                        .onErrorResume(AttemptNotFoundFault.class, ex -> Mono.empty())
                        .flatMap(attempt -> attemptService.newAttempt(attempt.getDeploymentId(),
                                          attempt.getStudentId(),
                                          attempt.getCoursewareElementType(),
                                          attempt.getCoursewareElementId(),
                                          attemptId,
                                          attempt.getValue() + 1)).block();

                if (lastAttempt != null) {
                    studentScopeService.resetScopesFor(event.getUpdateProgressEvent().getDeploymentId(),
                                                       evaluationEventMessage.getEvaluationActionState().getProgressActionContext().getElementId(),
                                                       event.getUpdateProgressEvent().getStudentId())
                            .blockLast();
                }

                // set the next current walkable based on the action context data
                return buildProgress(event, attemptId, completion, new WalkableChild()
                        .setElementId(actionContext.getElementId())
                        .setElementType(actionContext.getElementType()),
                        newCompletionValues, newCompletionConfidence);
            case INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE:
            case ACTIVITY_COMPLETE_AND_PATHWAY_COMPLETE:
                // set the completion to 1 and build a graph pathway progress with the same current walkable
                // as the previous progress
                return buildProgress(event, attemptId, new Completion()
                        .setValue(1f)
                        .setConfidence(1f), new WalkableChild()
                        .setElementId(previousProgress.getCurrentWalkableId())
                        .setElementType(previousProgress.getCurrentWalkableType()),
                        newCompletionValues,
                        newCompletionConfidence);
            default:
                // for any other action just build a normal progress
                return buildNormalProgress(event, attemptId, previousProgress, childrenWalkables,
                        newCompletionValues, newCompletionConfidence);
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childrenWalkables will be an empty list, not null")
    private Completion calculateCompletion(final Map<UUID, Float> newCompletionValues,
                                           final Map<UUID, Float> newCompletionConfidence,
                                           final List<WalkableChild> childrenWalkables) {
        return buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                childrenWalkables.size());
    }

    private GraphPathwayProgress buildProgress(@Nonnull final UpdateCoursewareElementProgressEvent event,
                                               @Nonnull final UUID attemptId,
                                               @Nonnull final Completion completion,
                                               @Nonnull final WalkableChild currentWalkable,
                                               @Nonnull final Map<UUID, Float> completionValues,
                                               @Nonnull final Map<UUID, Float> completionConfidence) {
        return new GraphPathwayProgress()
                .setId(UUIDs.timeBased())
                .setDeploymentId(event.getUpdateProgressEvent().getDeploymentId())
                .setChangeId(event.getUpdateProgressEvent().getChangeId())
                .setCoursewareElementId(event.getElement().getElementId())
                .setStudentId(event.getUpdateProgressEvent().getStudentId())
                .setAttemptId(attemptId)
                .setChildWalkableCompletionConfidences(completionValues)
                .setChildWalkableCompletionValues(completionConfidence)
                .setEvaluationId(event.getUpdateProgressEvent().getEvaluationId())
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
