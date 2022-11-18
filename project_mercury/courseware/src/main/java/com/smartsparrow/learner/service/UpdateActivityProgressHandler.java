package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.eval.service.ScenarioEvaluationService;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.StudentScope;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

@Singleton
public class UpdateActivityProgressHandler extends UpdateProgressHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateActivityProgressHandler.class);

    private final LearnerActivityService learnerActivityService;
    private final ProgressService progressService;
    private final AttemptService attemptService;
    private final LearnerScenarioService learnerScenarioService;
    private final ScenarioEvaluationService scenarioEvaluationService;
    private final StudentScopeService studentScopeService;
    private final CoursewareHistoryService coursewareHistoryService;

    @Inject
    protected UpdateActivityProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer,
                                            LearnerActivityService learnerActivityService,
                                            ProgressService progressService,
                                            AttemptService attemptService,
                                            LearnerScenarioService learnerScenarioService,
                                            ScenarioEvaluationService scenarioEvaluationService,
                                            StudentScopeService studentScopeService,
                                            CoursewareHistoryService coursewareHistoryService) {
        super(studentProgressRTMProducer);
        this.learnerActivityService = learnerActivityService;
        this.progressService = progressService;
        this.attemptService = attemptService;
        this.learnerScenarioService = learnerScenarioService;
        this.scenarioEvaluationService = scenarioEvaluationService;
        this.studentScopeService = studentScopeService;
        this.coursewareHistoryService = coursewareHistoryService;
    }

    @Handler
    @SuppressWarnings("Duplicates")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "the return value childPathways will be an empty list, not null")
    public void updateProgress(Exchange exchange) {

        final UpdateCoursewareElementProgressEvent event = exchange.getIn().getBody(UpdateCoursewareElementProgressEvent.class);
        EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        //
        // An activity's completion value:
        //  the sum of all the progress of the child pathways / # of child pathways.
        //

        final UUID activityId = event.getElement().getElementId();
        final UUID deploymentId = event.getUpdateProgressEvent().getDeploymentId();
        final UUID studentId = event.getUpdateProgressEvent().getStudentId();

        //find a corresponding activity attempt
        UUID childAttemptId = event.getEventProgress().get(event.getEventProgress().size() - 1).getAttemptId();
        Attempt attempt = attemptService.findById(childAttemptId).block();
        if (attempt == null) {
            throw new IllegalStateException("something is wrong. The attempt should exist");
        }
        final UUID attemptId = attempt.getParentId();

        // Load the previous progress; create if it doesn't exist as a placeholder.
        Mono<ActivityProgress> ppm = progressService.findLatestActivity(deploymentId, activityId, studentId);
        ActivityProgress previousProgress = ppm.block();
        if (previousProgress == null) {
            previousProgress = new ActivityProgress();
        }
        final Map<UUID, Float> prevComplValues = previousProgress.getChildWalkableCompletionValues();
        final Map<UUID, Float> prevComplConfidence = previousProgress.getChildWalkableCompletionConfidences();

        // find all the child pathways (to get their IDs) to find their progress.
        List<LearnerPathway> childPathways = learnerActivityService.findChildPathways(activityId, deploymentId)
                .collectList()
                .block();

        // Walk the children and aggregate in their completion values.
        // by re-walking the children (instead of copying the previous progress),
        // we keep the progress up to date if the courseware changes.
        Progress childProgress = event.getEventProgress().get(event.getEventProgress().size() - 1);
        Map<UUID, Float> newCompletionValues = merge(childPathways, prevComplValues,
                                                     childProgress.getCoursewareElementId(),
                                                     childProgress.getCompletion().getValue());
        Map<UUID, Float> newCompletionConfidence = merge(childPathways, prevComplConfidence,
                                                         childProgress.getCoursewareElementId(),
                                                         childProgress.getCompletion().getConfidence());

        // Perform the completion calculation
        Completion completion = buildCompletionData(newCompletionValues.values(), newCompletionConfidence.values(),
                                                    childPathways.size());

        final UUID parentPathwayId = learnerActivityService.findParentPathwayId(activityId, deploymentId)
                // default to an empty mono when the parentPathwayId is not found so we get a null on blocking
                .onErrorResume(LearnerPathwayNotFoundFault.class, ex -> Mono.empty())
                .block();


        // perform activity evaluation once the activity is marked as complete, as long as it is not a root level activity
        if(eventMessage != null && completion.isCompleted() && parentPathwayId != null) {
            //create the learner evaluation context from the deployment and studentId
            final EvaluationLearnerContext evaluationLearnerContext = new EvaluationLearnerContext()
                    .setDeploymentId(eventMessage.getDeployment().getId())
                    .setStudentId(studentId);

            //evaluate scenarios for the activity with scenario lifecycle ACTIVITY_COMPLETE
            List<ScenarioEvaluationResult> evaluationResults = learnerScenarioService.findAll(eventMessage.getDeployment(),
                                                                                              activityId,
                                                                                              ScenarioLifecycle.ACTIVITY_COMPLETE)
                    .concatMap(scenario -> scenarioEvaluationService.evaluateCondition(scenario,
                                                                                       evaluationLearnerContext))
                    .map(scenarioEvaluationResult -> scenarioEvaluationResult.setCoursewareElement(CoursewareElement.from(
                            activityId,
                            CoursewareElementType.ACTIVITY)))
                    .doOnEach(log.reactiveErrorThrowable("error while evaluating",
                                                         throwable -> new HashedMap<String, Object>() {
                                                             {
                                                                 put("activityId", activityId);
                                                                 put("studentId", studentId);
                                                             }
                                                         }))
                    .doOnError(ScenarioEvaluationException.class, cause -> {
                        throw new LearnerEvaluationException(cause.getMessage(), cause);
                    })
                    .takeUntil(scenarioEvaluationResult -> Boolean.TRUE.equals(scenarioEvaluationResult.getEvaluationResult()))
                    .collectList()
                    .block();

            try {
                //save the completed walkable for history purposes (only for non-root activities)
                coursewareHistoryService.record(event, attempt, parentPathwayId)
                        .block();
            } catch (LearnerPathwayNotFoundFault ex) {
                // Activity is the root element (no parent pathway found), so skip the recording step.
            }

            //add the scenario evaluation results to the existing list
            List<ScenarioEvaluationResult> evaluationResultList = eventMessage.getEvaluationResult().getScenarioEvaluationResults();
            evaluationResultList.addAll(evaluationResults);

            if (!evaluationResults.isEmpty()) {
                //get the actions and deserialize them for the scenario
                List<Action> actions = new ActionDeserializer()
                        .reactiveDeserialize(evaluationResults.get(evaluationResults.size() - 1).getActions())
                        .block();

                //find the first progress action
                Action progressAction = actions.stream()
                        .filter(action -> action.getType().equals(Action.Type.CHANGE_PROGRESS))
                        .findFirst()
                        .orElse(null);

                //when not null set the evaluation action state
                if (progressAction != null) {
                    //update the evaluation action state with current element and its progress action context instead of inheriting from the child
                    eventMessage.setEvaluationActionState(new EvaluationActionState()
                                    .setProgressActionContext((ProgressActionContext) progressAction.getContext())
                                    .setCoursewareElement(CoursewareElement.from(activityId, CoursewareElementType.ACTIVITY)));
                }
            }
        }

        /*
         * Progress on activity is calculated based on the progress action context and # of attempts
         */
        ActivityProgress progress;
        ProgressionType progressionType = null;
        //setting progressionType for the corresponding activity which has triggered the scenario evaluation
        if(eventMessage != null && eventMessage.getEvaluationActionState() != null && eventMessage.getEvaluationActionState().getCoursewareElement().getElementId().equals(activityId)) {
            progressionType = eventMessage.getEvaluationActionState().getProgressActionContext().getProgressionType();
        }

        if((ProgressionType.ACTIVITY_REPEAT).equals(progressionType)) {
            //Bump up the Activity
            Attempt newAttempt = attemptService.findById(attemptId)
                    .flatMap(activityAttempt -> attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.ACTIVITY, activityId,
                                                                          activityAttempt.getParentId(), activityAttempt.getValue() + 1))
                    .block();

            float value = 1 - (1.0f / newAttempt.getValue());
            float _confidence = 1 - (0.8f / newAttempt.getValue());
            float confidence = Math.min(0.9f, _confidence);

            Completion completionValue = new Completion().setValue(value).setConfidence(confidence);
            // build ACTIVITY_REPEAT progress with new attempt and completion value
            progress = buildActivityProgress(event, attemptId, completionValue, Maps.newHashMap(), Maps.newHashMap());
            //update scope downwards
            updateScopeDownward(activityId, deploymentId, studentId).block();
        } else {
            //build the normal progress
            progress = buildActivityProgress(event, attemptId, completion, newCompletionValues, newCompletionConfidence);
        }

        //persist the activity progress
        progressService.persist(progress)
                .singleOrEmpty()
                .block();

        /*
         * Propagate the progress, and calculate progress up the courseware tree.
         */

        // Broadcast the progress change.
        broadcastProgressEventMessage(progress, event.getUpdateProgressEvent());

        // Send it upward.
        propagateProgressChangeUpwards(exchange, event, progress);

    }

    private ActivityProgress buildActivityProgress(final UpdateCoursewareElementProgressEvent event,
                                                   final UUID attemptId,
                                                   final Completion completion,
                                                   final Map<UUID, Float> completionValues,
                                                   final Map<UUID, Float> completionConfidence) {
        return new ActivityProgress()
                .setId(UUIDs.timeBased())
                .setAttemptId(attemptId)
                .setChangeId(event.getUpdateProgressEvent().getChangeId())
                .setChildWalkableCompletionValues(completionValues)
                .setChildWalkableCompletionConfidences(completionConfidence)
                .setCompletion(completion)
                .setCoursewareElementId(event.getElement().getElementId())
                .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                .setDeploymentId(event.getUpdateProgressEvent().getDeploymentId())
                .setEvaluationId(event.getUpdateProgressEvent().getEvaluationId())
                .setStudentId(event.getUpdateProgressEvent().getStudentId());
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
     * @param activityId the activity to reset the scope from
     * @param deploymentId the deployment id
     * @param studentId the student to reset the scope for
     * @return a mono list of newly created student scopes or an empty list if no scopes required reset
     */
    private Mono<List<StudentScope>> updateScopeDownward(final UUID activityId, final UUID deploymentId, final UUID studentId) {
        return studentScopeService.resetScopesFor(deploymentId, activityId, studentId)
                .collectList();
    }
}
