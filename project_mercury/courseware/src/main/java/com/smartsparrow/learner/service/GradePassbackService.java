package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;
import org.joda.time.LocalDateTime;

import com.google.common.base.Strings;
import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.GradePassbackAssignment;
import com.smartsparrow.learner.data.GradePassbackGateway;
import com.smartsparrow.learner.data.GradePassbackItem;
import com.smartsparrow.learner.data.GradePassbackNotification;
import com.smartsparrow.learner.data.GradePassbackProgressType;
import com.smartsparrow.learner.data.LearnerScenarioGateway;
import com.smartsparrow.learner.outcome.GradePassbackRequestBuilder;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.wiring.GradePassbackConfig;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class GradePassbackService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GradePassbackService.class);
    private static final String BRONTE_INTERACTIVE_LAB = "BRONTE_INTERACTIVE_LAB";
    private static final String USERID_TYPE = "SMS";

    private final GradePassbackGateway gradePassbackGateway;
    private final ExternalHttpRequestService httpRequestService;
    private final StudentScoreService studentScoreService;
    private final ProgressService progressService;
    private final DeploymentService deploymentService;
    private final AttemptService attemptService;

    private final IesSystemToSystemIdentityProvider iesSystemToSystemIdentityProvider;
    private final LearnerScenarioGateway learnerScenarioGateway;
    private final ActionDeserializer actionDeserializer;
    private final DeploymentGateway deploymentGateway;
    private final CohortGateway cohortGateway;
    private final GradePassbackConfig gradePassbackConfig;

    @Inject
    public GradePassbackService(final GradePassbackGateway gradePassbackGateway,
                                final ExternalHttpRequestService httpRequestService,
                                final StudentScoreService studentScoreService,
                                final ProgressService progressService,
                                final DeploymentService deploymentService,
                                final AttemptService attemptService,
                                final IesSystemToSystemIdentityProvider iesSystemToSystemIdentityProvider,
                                final LearnerScenarioGateway learnerScenarioGateway,
                                final ActionDeserializer actionDeserializer,
                                final DeploymentGateway deploymentGateway,
                                final CohortGateway cohortGateway,
                                final GradePassbackConfig gradePassbackConfig) {
        this.gradePassbackGateway = gradePassbackGateway;
        this.httpRequestService = httpRequestService;
        this.studentScoreService = studentScoreService;
        this.progressService = progressService;
        this.deploymentService = deploymentService;
        this.attemptService = attemptService;
        this.iesSystemToSystemIdentityProvider = iesSystemToSystemIdentityProvider;
        this.learnerScenarioGateway = learnerScenarioGateway;
        this.actionDeserializer = actionDeserializer;
        this.deploymentGateway = deploymentGateway;
        this.cohortGateway = cohortGateway;
        this.gradePassbackConfig = gradePassbackConfig;
    }

    /**
     * Saves (asynchronously) a courseware grade outcome via an external outcome service.  Returns the outstanding
     * ext-http RequestNotification associated with this request.
     *
     * @param deploymentId the deployment id associated with this outcome
     * @param changeId the change id associated with this outcome
     * @param studentId the student id associated with this outcome
     * @param coursewareElement the courseware element associated with this outcome
     * @param outcomeServiceUrl the external outcome service url
     * @param gradePassbackAssignment the assignment grade data to update
     * @return a mono with the request notification summary
     */
    public Mono<RequestNotification> saveOutcome(UUID deploymentId,
                                                 UUID changeId,
                                                 UUID studentId,
                                                 CoursewareElement coursewareElement,
                                                 String outcomeServiceUrl,
                                                 GradePassbackAssignment gradePassbackAssignment) {
        log.info("saveOutcome() :: gradePassbackAssignment:: " + gradePassbackAssignment.toString());
        final UUID referenceId = UUIDs.timeBased();
        return saveOutcome(referenceId, outcomeServiceUrl, gradePassbackAssignment)
                .flatMap(requestNotification -> {
                    UUID notificationId = requestNotification.getState().getNotificationId();
                    GradePassbackNotification gradePassbackNotification = new GradePassbackNotification()
                            .setNotificationId(notificationId)
                            .setDeploymentId(deploymentId)
                            .setChangeId(changeId)
                            .setStudentId(studentId)
                            .setCoursewareElementId(coursewareElement.getElementId())
                            .setCoursewareElementType(coursewareElement.getElementType())
                            // todo .setResultScore(resultScore) review notification table schema by BRNT-7735
                            .setStatus(GradePassbackNotification.Status.SENDING);

                    return gradePassbackGateway.persist(gradePassbackNotification)
                            .then(Mono.just(requestNotification));
                });
    }

    /**
     * Saves (asynchronously) a courseware grade outcome via an external outcome service.  Returns the outstanding
     * ext-http RequestNotification associated with this request.
     *
     * @param referenceId the reference id associated with this request
     * @param outcomeServiceUrl the external outcome service url
     * @param gradePassbackAssignment the assignment grade data to update
     * @return a mono with the request notification summary
     */
    Mono<RequestNotification> saveOutcome(UUID referenceId,
                                          String outcomeServiceUrl,
                                          GradePassbackAssignment gradePassbackAssignment) {
        log.info("saveOutcome() :: " + referenceId + ", " + outcomeServiceUrl);
        log.info("saveOutcome() :: gradePassbackAssignment :: " + gradePassbackAssignment);
        affirmArgument(referenceId != null, "referenceId is required");
        affirmArgument(!Strings.isNullOrEmpty(outcomeServiceUrl), "outcomeServiceUrl is required");
        affirmArgument(gradePassbackAssignment != null, "gradePassbackAssignment is required");

        // prepare builder with common fields
        GradePassbackRequestBuilder reqBuilder = new GradePassbackRequestBuilder()
                .setUri(outcomeServiceUrl)
                .setGradePassbackAssignment(gradePassbackAssignment);

        try {
            reqBuilder.setPiToken(iesSystemToSystemIdentityProvider.getPiToken()); // set IES system token
        } catch (UnsupportedEncodingException | AutobahnIdentityProviderException e) {
            throw new RuntimeException("GradePassBack Service - Unable to set IES system token");
        }

        return Mono
                // build request
                .just(reqBuilder.build())
                // send it
                .flatMap(request -> httpRequestService.submit(RequestPurpose.GRADE_PASSBACK, request, referenceId))
                .doOnEach(log.reactiveInfoSignal("sent outcome score to outcome service"));
    }

    /**
     * Find gradepassback assignment and item by deployment id, user id, and element id
     *
     * @param deploymentId the deployment id
     * @param userId       the user id
     * @param elementId    the element id
     * @param ltiData     the lti data
     * @return a mono of gradepassback assignment
     */
    public Mono<GradePassbackAssignment> getGradePassbackAssignment(UUID deploymentId,
                                                                    UUID userId,
                                                                    UUID elementId,
                                                                    LTIData ltiData) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(userId != null, "userId is required");
        affirmArgument(elementId != null, "elementId is required");

        // checking enrollment type
        // Need to analyze this logic can be moved to handler
        return deploymentGateway.findLatest(deploymentId).map(DeployedActivity::getCohortId)
                .switchIfEmpty(Mono.error(new NotFoundFault("cannot find cohort id") ))
                .flatMap(cohortId -> cohortGateway.findCohortSummary(cohortId)
                        .switchIfEmpty(Mono.error(new NotFoundFault("cannot find cohort summary") ))
                        .map(cohortSummary -> {
                    if (cohortSummary.getType() != null && cohortSummary.getType().equals(EnrollmentType.LTI)) {
                        if (ltiData == null) {
                            log.error("Error processing GRADE_PASSBACK action. ltiData is null");
                            throw new IllegalStateFault(
                                    "error processing GRADE_PASSBACK action. ltiData is null");
                        }

                        if (ltiData.getAssignmentId() == null || ltiData.getCourseId() == null || ltiData.getUserId() == null || ltiData.getAttemptLimit() == null || ltiData.getCustomGradingMethod() == null
                                || ltiData.getDiscipline() == null) {
                            log.error("Error processing GRADE_PASSBACK action. One or more LtiParams are null");
                            throw new IllegalStateFault(
                                    "error processing GRADE_PASSBACK action. One or more LtiParams are null");
                        }
                    }
                    return cohortSummary.getType();
                }))
                .flatMap(enrollmentTypeMono -> {
                    String customGradingMethod = null != ltiData ? ltiData.getCustomGradingMethod() : null;

                    return getAssignmentScoreAndProgress(deploymentId,
                                                         userId,
                                                         customGradingMethod)
                            .flatMap(gradePassbackAssignment -> getAssignmentItemScoreAndProgress(deploymentId,
                                                                                                  userId,
                                                                                                  elementId,
                                                                                                  customGradingMethod)
                                    .map(gradePassbackItem -> {
                                        List<GradePassbackItem> items = new ArrayList<>();
                                        items.add(gradePassbackItem);

                                        return null != ltiData ?
                                                populateGradePassbackAssignment(gradePassbackAssignment)
                                                        .setUserId(ltiData.getUserId())
                                                        .setAssignmentId(ltiData.getAssignmentId())
                                                        .setCourseId(ltiData.getCourseId())
                                                        .setItemScore(items)
                                                : populateGradePassbackAssignment(
                                                gradePassbackAssignment).setItemScore(
                                                items);
                                    })
                            );
                });

    }

    /**
     * Find gradepassback assignment by deployment id and user id
     *
     * @param deploymentId the deployment id
     * @param userId       the user id
     * @param customGradingMethod    the custom grading method: accuracy and completion
     * @return a mono of gradepassback assignment
     * @throws NotFoundFault if deployment cannot be found
     */
    public Mono<GradePassbackAssignment> getAssignmentScoreAndProgress(UUID deploymentId, UUID userId, String customGradingMethod) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(userId != null, "userId is required");

        return deploymentService.findDeployment(deploymentId)
                // throw not found error if no deployment found
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find deployment by id: %s", deploymentId))))
                // get assignment attempt value
                .flatMap(deployment -> attemptService.findLatestAttempt(deploymentId, deployment.getActivityId(), userId)
                        // set attempt to 0 if no attempt found
                        .switchIfEmpty(Mono.just(new Attempt()
                                                    .setCoursewareElementId(deployment.getActivityId())
                                                    .setValue(0))))
                // get assignment progress
                .flatMap(attempt -> progressService.findLatestActivity(deploymentId, attempt.getCoursewareElementId(), userId)
                        .switchIfEmpty(Mono.just(new ActivityProgress()))
                        .map(assignmentProgress -> {
                                Float progress = 0f; // set progress to 0 if no activity progress found

                                // TODO: get completion value from child_completion_values for now. Need to fina a better way to get assignment completion values
                                if (assignmentProgress.getChildWalkableCompletionValues() != null) {
                                    for(float value : assignmentProgress.getChildWalkableCompletionValues().values()){
                                        progress += value;
                                    }
                                }

                                return new GradePassbackAssignment()
                                        .setRootElementId(assignmentProgress.getCoursewareElementId())
                                        .setAttemptNo(attempt.getValue())
                                        .setAssignmentProgress(getGradePassbackProgressType(progress))
                                        .setAssignmentProgressPercentage(Math.round(progress * 100));
                        }))
                .flatMap(gradePassbackAssignment -> {
                    if(customGradingMethod != null && customGradingMethod.equalsIgnoreCase("completion")) {
                        return Mono.just(gradePassbackAssignment.setAssignmentScore(gradePassbackAssignment.getAssignmentProgressPercentage().floatValue()));
                    }
                    //TO DO commented out the following code for now but enable it later if required
                    // return 0 if the assignment is not completed
//                    if (gradePassbackAssignment.getAssignmentProgress() != GradePassbackProgressType.COMPLETED) {
//                        return Mono.just(gradePassbackAssignment.setAssignmentScore(0f));
//                    }
                    // get assignment score
                    return studentScoreService.computeScore(deploymentId, userId, gradePassbackAssignment.getRootElementId(), null)
                            .map(score -> gradePassbackAssignment.setAssignmentScore(score.getValue().floatValue()));
                });
    }

    /**
     * Find a gradepassback item by deployment id, user id, and element id
     *
     * @param deploymentId  the deployment id
     * @param userId        the user id
     * @param elementId     the element id
     * @param customGradingMethod    the custom grading method: accuracy and completion
     * @return a mono of gradepassback item
     */
    public Mono<GradePassbackItem> getAssignmentItemScoreAndProgress(UUID deploymentId, UUID userId, UUID elementId, String customGradingMethod) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(userId != null, "userId is required");
        affirmArgument(elementId != null, "elementId is required");

        return progressService.findLatest(deploymentId, elementId, userId)// get assignment item progress
                .switchIfEmpty(Mono.just(new GeneralProgress()))
                .map(itemProgress -> {
                        Float progress = 0f; // set progress to 0 if no activity progress found

                        if (itemProgress.getCompletion() != null) {
                            //TODO: set progress to 100 for now. Need to find a better way to get item completion value
                            progress = 1f;
                        }

                        return new GradePassbackItem()
                                .setItemProgress(getGradePassbackProgressType(progress))
                                .setItemProgressPercentage(Math.round(progress * 100))
                                .setItemURN(itemProgress.getId() != null ? "/items/" + itemProgress.getId() : "/items/" + UUID.randomUUID())
                                .setItemId(itemProgress.getId() != null ? itemProgress.getId().hashCode() : UUID.randomUUID().hashCode())
                                .setCallerCode(BRONTE_INTERACTIVE_LAB)
                                .setProgressDateTime(LocalDateTime.now().toDate().toInstant().getEpochSecond());
                })
                .flatMap(gradePassbackItem -> {
                    if (customGradingMethod != null && customGradingMethod.equalsIgnoreCase("completion")) {
                        if (gradePassbackItem.getItemProgress() == GradePassbackProgressType.completed) {
                            return getGradePassbackActionCountCheck(deploymentId)
                                    .map(totalQuestionCount -> {
                                        float score = 0f;
                                        if (totalQuestionCount != 0) {
                                            score = (100 / totalQuestionCount.floatValue());
                                        }
                                        return gradePassbackItem.setScore(score);
                                    });
                        }
                        return Mono.just(gradePassbackItem.setScore(0f));
                    }

                    // TODO commented out the following code for now but enable it later if required
                    // return 0 if the assignment item is not completed
//                    if (gradePassbackItem.getItemProgress() != GradePassbackProgressType.COMPLETED) {
//                        return Mono.just(gradePassbackItem.setScore(0f));
//                    }

                    // get assignment item score
                    return studentScoreService.computeScore(deploymentId, userId, elementId, null)
                            .map(score -> gradePassbackItem.setScore(score.getValue().floatValue()));
                });
    }

    /**
     * Get total number of questions in the assignment
     *
     * @param deploymentId
     * @return mono of total question
     * @throws NotFoundFault if deployment cannot be found
     */
    public Mono<Integer> getTotalGradePassbackActionCount(UUID deploymentId) {
        affirmArgument(deploymentId != null, "deploymentId is required");

        Set<UUID> elementIdSet = new HashSet<>();

        return deploymentService.findDeployment(deploymentId) // get latest change id for the deployment
                // throw not found error if no deployment found
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find deployment by id: %s", deploymentId))))
                // get all scenarios by deployment id and change id
                .flatMapMany(deployment -> {
                    return learnerScenarioGateway.fetchAllById(deploymentId, deployment.getChangeId());
                })
                .flatMap(learnerScenario ->
                        // get actions from scenario
                        actionDeserializer.reactiveDeserialize(learnerScenario.getActions())
                                .map(actions -> {
                                            actions.stream()
                                                    .filter(action -> action.getType().equals(Action.Type.GRADE_PASSBACK)) // get all grade passback actions
                                                    .forEach(gradePassbackAction -> {
                                                        UUID elementId = ((GradePassbackAction) gradePassbackAction).getContext()
                                                                .getElementId(); // get grade passback element id
                                                        elementIdSet.add(elementId);
                                                    });
                                            return elementIdSet.size();
                                        }
                                )
                ).last();
    }

    /**
     * Find gradepassback progress type by progress value
     *
     * @param progressValue
     * @return gradepassback progress type
     */
    private GradePassbackProgressType getGradePassbackProgressType(Float progressValue) {
        log.info("getGradePassbackProgressType() :: progressValue :: " + progressValue);
        return progressValue.equals(1f) ? GradePassbackProgressType.completed :
                progressValue.equals(0f) ? GradePassbackProgressType.not_started : GradePassbackProgressType.inprogress;
    }

    private GradePassbackAssignment populateGradePassbackAssignment(GradePassbackAssignment gradePassbackAssignment) {

        return gradePassbackAssignment
                .setUserIdType(USERID_TYPE)
                .setAssignmentCallerCode(BRONTE_INTERACTIVE_LAB)
                .setCallerCode(BRONTE_INTERACTIVE_LAB)
                .setAssignmentProgressDateTime(LocalDateTime.now().toDate().toInstant().getEpochSecond());
    }

    /**
     * Get total number of questions in the assignment
     *
     * @param deploymentId
     * @return mono of total question
     * @throws NotFoundFault if deployment cannot be found
     */
    public Mono<Integer> getGradePassbackActionCount(UUID deploymentId) {
        affirmArgument(deploymentId != null, "deploymentId is required");

        log.jsonInfo("fetching GradePassbackAction Count", new HashedMap<String, Object>() {
            {
                put("deploymentId", deploymentId);
            }
        });
        return deploymentService.findDeployment(deploymentId) // get latest change id for the deployment
                // throw not found error if no deployment found
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find deployment by id: %s",
                                                                          deploymentId))))
                .flatMap(deployedActivity -> {
                    return deploymentGateway.findGradepassbackCount(deploymentId,
                                                                             deployedActivity.getChangeId());
                }).map(count ->{
                   return count.intValue();
                });
    }

    public  Mono<Integer> getGradePassbackActionCountCheck(UUID deploymentId) {

        Boolean checkByMetaData = gradePassbackConfig.isCheckGradePassbackQuestionCount();
        if(checkByMetaData != null && checkByMetaData){
            log.jsonInfo("fetching GradePassbackAction questions count by metadata", new HashedMap<String, Object>() {
                {
                    put("deploymentId", deploymentId);
                }
            });

            return  getGradePassbackActionCount(deploymentId);
        }
        return getTotalGradePassbackActionCount(deploymentId);
    }
}
