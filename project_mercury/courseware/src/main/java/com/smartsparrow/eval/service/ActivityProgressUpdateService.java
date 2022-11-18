package com.smartsparrow.eval.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.StudentScope;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerScenarioService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

/**
 * Implement the logic for updating the progress for an activity.
 * The implementation is taken from {@link com.smartsparrow.learner.service.UpdateActivityProgressHandler}
 * FIXME important! this class does not support a direct activity evaluation. This will need to be refactored
 * to handle the case (there are no previous progresses for instance, this will be the first progress generated)
 */
@Singleton
public class ActivityProgressUpdateService implements ProgressUpdateService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityProgressUpdateService.class);

    private final LearnerActivityService learnerActivityService;
    private final ProgressService progressService;
    private final AttemptService attemptService;
    private final LearnerScenarioService learnerScenarioService;
    private final ScenarioEvaluationService scenarioEvaluationService;
    private final StudentScopeService studentScopeService;
    private final CoursewareHistoryService coursewareHistoryService;
    private final ActionDeserializer actionDeserializer;

    @Inject
    public ActivityProgressUpdateService(final LearnerActivityService learnerActivityService,
                                         final ProgressService progressService,
                                         final AttemptService attemptService,
                                         final LearnerScenarioService learnerScenarioService,
                                         final ScenarioEvaluationService scenarioEvaluationService,
                                         final StudentScopeService studentScopeService,
                                         final CoursewareHistoryService coursewareHistoryService,
                                         final ActionDeserializer actionDeserializer) {
        this.learnerActivityService = learnerActivityService;
        this.progressService = progressService;
        this.attemptService = attemptService;
        this.learnerScenarioService = learnerScenarioService;
        this.scenarioEvaluationService = scenarioEvaluationService;
        this.studentScopeService = studentScopeService;
        this.coursewareHistoryService = coursewareHistoryService;
        this.actionDeserializer = actionDeserializer;
    }

    @Trace(async = true)
    @Override
    public Mono<Progress> updateProgress(CoursewareElement element, ProgressAction action, LearnerEvaluationResponseContext responseContext) {
        //
        // An activity's completion value:
        //  the sum of all the progress of the child pathways / # of child pathways.
        //

        final LearnerEvaluationResponse response = responseContext.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();
        final Deployment deployment = request.getDeployment();

        final UUID activityId = element.getElementId();
        final UUID deploymentId = deployment.getId();
        final UUID studentId = request.getStudentId();
        // find a corresponding activity attempt
        // ATTENTION! this currently works because we are evaluating only interactives,
        // the following two lines WILL BREAK when we have to start supporting activity evaluation via api call, find a solution then
        final Progress childProgress = responseContext.getProgresses().get(responseContext.getProgresses().size() - 1);
        final UUID childAttemptId = childProgress.getAttemptId();

        // find the attempt
        Mono<Attempt> attemptMono = attemptService.findById(childAttemptId);
        // find the previous progress
        Mono<ActivityProgress> previousProgressMono = progressService.findLatestActivity(deploymentId, activityId, studentId)
                .defaultIfEmpty(new ActivityProgress());
        // find all the child pathways (to get their IDs) to find their progress.
        Mono<List<LearnerPathway>> childPathwaysMono = learnerActivityService.findChildPathways(activityId, deploymentId)
                .collectList();

        // zip the sources together
        return Mono.zip(attemptMono, previousProgressMono, childPathwaysMono)
                .flatMap(tuple3 -> {
                    final Attempt attempt = tuple3.getT1();
                    final ActivityProgress previousProgress = tuple3.getT2();
                    final List<LearnerPathway> childPathways = tuple3.getT3();
                    final UUID attemptId = attempt.getParentId();

                    // get the prev completion values
                    final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
                    final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

                    // compute the new completion values
                    Map<UUID, Float> newCompletionValues = merge(childPathways, prevComplValues,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getValue());
                    Map<UUID, Float> newCompletionConfidence = merge(childPathways, prevComplConfidence,
                            childProgress.getCoursewareElementId(),
                            childProgress.getCompletion().getConfidence());

                    // build the completion data
                    Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                            childPathways.size());

                    return Mono.just(completion.isCompleted())
                            // if this is completed and not a root level activity, let's try trigger an activity evaluation
                            .flatMap(isCompleted -> {
                                if (isCompleted && !isARootLevelActivity(element, responseContext.getAncestry())) {
                                    // alright we need to evaluate!
                                    return evaluateActivity(responseContext, element, attempt)
                                            .defaultIfEmpty(new CompletedWalkable());
                                }
                                // no need to evaluate, return an empty completed walkable that will be ignored
                                return Mono.just(new CompletedWalkable());
                            })
                            // ignore the previous returned type
                            .flatMap(ignored -> {
                                // build the activity progress
                                return getActivityProgress(activityId, responseContext, attemptId, newCompletionValues,
                                        newCompletionConfidence, completion)
                                        .flatMap(progress -> {
                                            // persist the activity progress
                                            return progressService.persist(progress)
                                                    .then(Mono.just(progress));
                                        });
                            });
                });
    }

    /**
     * Find out if the element is a root element
     *
     * @param element  the element to figure out if it is a root activity or not
     * @param ancestry a list of courseware elements representing the ancestry from the evaluated element to the root level activity
     * @return <code>true</code> if the element is the root activity; <code>false</code> when not
     */
    private boolean isARootLevelActivity(CoursewareElement element, List<CoursewareElement> ancestry) {
        // get the last element from the ancestry
        CoursewareElement rootElement = ancestry.get(ancestry.size() - 1);
        return element.equals(rootElement);
    }

    private Mono<ActivityProgress> getActivityProgress(final UUID activityId,
                                                       final LearnerEvaluationResponseContext context,
                                                       final UUID attemptId,
                                                       final Map<UUID, Float> newCompletionValues,
                                                       final Map<UUID, Float> newCompletionConfidence,
                                                       final Completion completion) {
        final LearnerEvaluationRequest request = context.getResponse()
                .getEvaluationRequest();
        final Deployment deployment = request.getDeployment();
        final UUID studentId = request.getStudentId();
        /*
         * Progress on activity is calculated based on the progress action context and # of attempts
         */
        ProgressionType progressionType = null;
        // setting progressionType for the corresponding activity which has triggered the scenario evaluation
        if (context.getEvaluationActionState() != null && context.getEvaluationActionState().getCoursewareElement().getElementId().equals(activityId)) {
            progressionType = context.getEvaluationActionState().getProgressActionContext().getProgressionType();
        }

        if ((ProgressionType.ACTIVITY_REPEAT).equals(progressionType)) {
            // Bump up the Activity

            return attemptService.findById(attemptId)
                    .flatMap(activityAttempt -> attemptService.newAttempt(deployment.getId(), studentId, CoursewareElementType.ACTIVITY, activityId,
                            activityAttempt.getParentId(), activityAttempt.getValue() + 1))
                    .flatMap(newAttempt -> {
                        float value = 1 - (1.0f / newAttempt.getValue());
                        float _confidence = 1 - (0.8f / newAttempt.getValue());
                        float confidence = Math.min(0.9f, _confidence);

                        Completion completionValue = new Completion().setValue(value).setConfidence(confidence);
                        // build ACTIVITY_REPEAT progress with new attempt and completion value
                        ActivityProgress progress = buildActivityProgress(activityId, context, attemptId, completionValue, Maps.newHashMap(), Maps.newHashMap());
                        //update scope downwards
                        return updateScopeDownward(activityId, deployment.getId(), studentId)
                                .then(Mono.just(progress));
                    });
        } else {
            // build the normal progress
            return Mono.just(buildActivityProgress(activityId, context, attemptId, completion, newCompletionValues, newCompletionConfidence));
        }
    }

    /**
     * Evaluate the activity and returns a completed walkable
     *
     * @param context  the evaluation context holding all the evaluation information
     * @param activity the activity to evaluate
     * @return a mono including either the persisted completed walkable or an empty mono
     */
    private Mono<CompletedWalkable> evaluateActivity(LearnerEvaluationResponseContext context, CoursewareElement activity, Attempt attempt) {
        final LearnerEvaluationResponse response = context.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();
        final Deployment deployment = request.getDeployment();

        //create the learner evaluation context from the deployment and studentId
        final EvaluationLearnerContext evaluationLearnerContext = new EvaluationLearnerContext()
                .setDeploymentId(deployment.getId())
                .setStudentId(request.getStudentId());

        // evaluate scenarios for the activity with scenario lifecycle ACTIVITY_COMPLETE
        return learnerScenarioService.findAll(deployment, activity.getElementId(),
                ScenarioLifecycle.ACTIVITY_COMPLETE)
                .concatMap(scenario -> scenarioEvaluationService.evaluateCondition(scenario,
                        evaluationLearnerContext))
                .map(scenarioEvaluationResult -> scenarioEvaluationResult.setCoursewareElement(CoursewareElement.from(
                        activity.getElementId(),
                        CoursewareElementType.ACTIVITY)))
                .doOnEach(log.reactiveErrorThrowable("error while evaluating",
                        throwable -> new HashedMap<String, Object>() {
                            {
                                put("activityId", activity.getElementId());
                                put("studentId", request.getStudentId());
                            }
                        }))
                .doOnError(ScenarioEvaluationException.class, cause -> {
                    throw new LearnerEvaluationException(cause.getMessage(), cause);
                })
                .takeUntil(scenarioEvaluationResult -> Boolean.TRUE.equals(scenarioEvaluationResult.getEvaluationResult()))
                .collectList()
                .flatMap(scenarioEvaluationResults -> {
                    // deserialize all those new actions
                    List<Action> actions = scenarioEvaluationResults.parallelStream()
                            .map(scenarioEvaluationResult -> actionDeserializer.deserialize(scenarioEvaluationResult.getActions()))
                            .reduce((accumulator, combiner) -> {
                                accumulator.addAll(combiner);
                                return accumulator;
                            })
                            .orElse(new ArrayList<>());

                    // find the first progress action
                    Action progressAction = actions.stream()
                            .filter(action -> action.getType().equals(Action.Type.CHANGE_PROGRESS))
                            .findFirst()
                            .orElse(null);

                    // when not null set the evaluation action state
                    if (progressAction != null) {
                        // update the evaluation action state with current element and its progress action context instead of inheriting from the child
                        context.setEvaluationActionState(new EvaluationActionState()
                                .setProgressActionContext((ProgressActionContext) progressAction.getContext())
                                .setCoursewareElement(activity));
                    }

                    // add all the triggered actions to the context
                    response.getWalkableEvaluationResult().getTriggeredActions().addAll(actions);
                    // add the scenarios to the evaluated scenarios list
                    response.getScenarioEvaluationResults().addAll(scenarioEvaluationResults);
                    // record the walkable for history purposes, we need to find the parent pathway first
                    final UUID pathwayId = parentFrom(activity, context.getAncestry());
                    return coursewareHistoryService.record(response.getWalkableEvaluationResult().getId(), request, activity, attempt, pathwayId);
                });
    }

    /**
     * Find the parent element for this activity using the ancestry list
     *
     * @param activity the activity to find the parent for
     * @param ancestry the ancestry to look for the parent at
     * @return the parent pathway id
     * @throws NotFoundFault when the parent pathway is not found within the ancestry (this should never happen in this
     * context and therefore the fault, if it happens is not recoverable)
     */
    public UUID parentFrom(final CoursewareElement activity, final List<CoursewareElement> ancestry) {
        // ancestry is always from bottom
        Iterator<CoursewareElement> iterator = ancestry.iterator();

        while (iterator.hasNext()) {
            CoursewareElement current = iterator.next();
            if (current.equals(activity)) {
                return iterator.next().getElementId();
            }
        }

        throw new NotFoundFault(String.format("parent pathway not found for %s", activity));
    }

    private ActivityProgress buildActivityProgress(final UUID activityId,
                                                   final LearnerEvaluationResponseContext context,
                                                   final UUID attemptId,
                                                   final Completion completion,
                                                   final Map<UUID, Float> completionValues,
                                                   final Map<UUID, Float> completionConfidence) {
        final LearnerEvaluationResponse response = context.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();
        final Deployment deployment = request.getDeployment();

        return new ActivityProgress()
                .setId(UUIDs.timeBased())
                .setAttemptId(attemptId)
                .setChangeId(deployment.getChangeId())
                .setChildWalkableCompletionValues(completionValues)
                .setChildWalkableCompletionConfidences(completionConfidence)
                .setCompletion(completion)
                .setCoursewareElementId(activityId)
                .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                .setDeploymentId(deployment.getId())
                .setEvaluationId(response.getWalkableEvaluationResult().getId())
                .setStudentId(request.getStudentId());
    }

    Map<UUID, Float> merge(final List<LearnerPathway> childPathways,
                           final Map<UUID, Float> prevValues,
                           final UUID childElementId,
                           final Float childValue) {
        //
        Map<UUID, Float> ret = childPathways.stream() //
                .filter(childPathway -> prevValues.containsKey(childPathway.getId()))
                .filter(childPathway -> prevValues.get(childPathway.getId()) != null)
                .collect(Collectors.toMap(LearnerPathway::getId, childPathway -> prevValues.get(childPathway.getId())));

        if (childValue != null) {
            ret.put(childElementId, childValue);
        }

        return ret;
    }

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
        // sanity check.
        progressValue = Math.min(progressValue, 1.0f);
        confidenceValue = Math.min(confidenceValue, 1.0f);

        // build the completion
        return new Completion().setValue(progressValue).setConfidence(confidenceValue);
    }

    /**
     * Reset the initialised scope in the subtree for the supplied activity. Create a new {@link StudentScope} for all
     * the initialised student scope present in the subtree.
     *
     * @param activityId   the activity to reset the scope from
     * @param deploymentId the deployment id
     * @param studentId    the student to reset the scope for
     * @return a mono list of newly created student scopes or an empty list if no scopes required reset
     */
    private Mono<List<StudentScope>> updateScopeDownward(final UUID activityId, final UUID deploymentId, final UUID studentId) {
        return studentScopeService.resetScopesFor(deploymentId, activityId, studentId)
                .collectList();
    }
}
