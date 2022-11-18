package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.feedback.SendFeedbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.GradePassbackAssignment;
import com.smartsparrow.learner.data.GradePassbackItem;
import com.smartsparrow.learner.data.GradePassbackProgressType;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerScenarioGateway;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.wiring.GradePassbackConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class GradePassbackServiceTest {

    @InjectMocks
    private GradePassbackService gradePassbackService;

    @Mock
    private StudentScoreService studentScoreService;

    @Mock
    ProgressService progressService;

    @Mock
    DeploymentService deploymentService;

    @Mock
    AttemptService attemptService;

    @Mock
    LearnerScenarioGateway learnerScenarioGateway;

    @Mock
    ActionDeserializer actionDeserializer;

    @Mock
    DeploymentGateway deploymentGateway;

    @Mock
    CohortGateway cohortGateway;

    LTIData ltiData;

    @Mock
    GradePassbackConfig gradePassbackConfig;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID userId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();

    private DeployedActivity deployedActivity;
    private CohortSummary cohortSummary;
    private ActivityProgress activityProgress;
    private GeneralProgress generalProgress;
    private final static Integer scoreValue = 90;
    private final static int attemptValue = 1;
    private Map<UUID, Float> childCompletionValues = new HashedMap();

    private final GradePassbackAction gradePassbackAction1 = new GradePassbackAction()
            .setContext(new GradePassbackActionContext().setElementId(UUID.randomUUID()));
    private final GradePassbackAction gradePassbackAction2 = new GradePassbackAction()
            .setContext(new GradePassbackActionContext().setElementId(UUID.randomUUID()));
    private final ChangeScoreAction changeScoreAction = new ChangeScoreAction();
    private final SendFeedbackAction sendFeedbackAction =  new SendFeedbackAction().setType(Action.Type.SEND_FEEDBACK);

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        deployedActivity = new DeployedActivity().setActivityId(rootElementId).setChangeId(changeId);
        activityProgress = new ActivityProgress().setDeploymentId(deploymentId);
        generalProgress = new GeneralProgress().setId(UUID.randomUUID());

        when(deploymentService.findDeployment(deploymentId))
                .thenReturn(Mono.just(deployedActivity));
        when(attemptService.findLatestAttempt(deploymentId, rootElementId, userId))
                .thenReturn(Mono.just(new Attempt().setCoursewareElementId(rootElementId)
                        .setValue(attemptValue)));
        when(attemptService.findLatestAttempt(deploymentId, elementId, userId))
                .thenReturn(Mono.just(new Attempt().setCoursewareElementId(elementId)
                        .setValue(attemptValue)));
        when(studentScoreService.computeScore(deploymentId, userId, rootElementId, null))
                .thenReturn(Mono.just(new Score().setValue(scoreValue.doubleValue())));
        when(studentScoreService.computeScore(deploymentId, userId, elementId, null))
                .thenReturn(Mono.just(new Score().setValue(scoreValue.doubleValue())));

        ltiData = new LTIData();
        ltiData.setUserId("12345").setAssignmentId(5657576).setCourseId("797978979").setAttemptLimit(2)
                .setCustomGradingMethod("ungraded")
                .setDiscipline("MasteringBiology");


        deployedActivity.setCohortId(cohortId);
        when(deploymentGateway.findLatest(deploymentId)).thenReturn(Mono.just(deployedActivity));

        cohortSummary = new CohortSummary();
        cohortSummary.setType(EnrollmentType.LTI);
        when(cohortGateway.findCohortSummary(cohortId)).thenReturn(Mono.just(cohortSummary));
    }

    @Test
    void getAssignmentScoreAndProgress_noDeploymentId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getAssignmentScoreAndProgress(null, userId, null));

        assertEquals("deploymentId is required", ex.getMessage());
    }

    @Test
    void getAssignmentScoreAndProgress_noUserId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getAssignmentScoreAndProgress(deploymentId, null, null));

        assertEquals("userId is required", ex.getMessage());
    }

    @Test
    void getAssignmentScoreAndProgress_noDeploymentFound() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.empty());

        NotFoundFault ex = assertThrows(NotFoundFault.class,
                () -> gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block());

        assertTrue(ex.getMessage().contains("cannot find deployment by id: " + deploymentId));
    }

    @Test
    void getAssignmentScoreAndProgress_notStarted() {
        when(attemptService.findLatestAttempt(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.empty());
        when(progressService.findLatestActivity(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.empty());
        when(studentScoreService.computeScore(eq(deploymentId), eq(userId), any(), eq(null)))
                .thenReturn(Mono.just(new Score().setValue(0.0)));

        GradePassbackAssignment result =
                gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.not_started, result.getAssignmentProgress());
        assertEquals(0f, result.getAssignmentScore());
        assertEquals(0, result.getAssignmentProgressPercentage());
        assertEquals(0, result.getAttemptNo());
    }

    @Test
    void getAssignmentScoreAndProgress_inProgress() {
        childCompletionValues.put(UUID.randomUUID(), .5f);
        when(progressService.findLatestActivity(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(deployedActivity.getActivityId())
                                        .setChildWalkableCompletionValues(childCompletionValues)));

        GradePassbackAssignment result =
                gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.inprogress, result.getAssignmentProgress());
        assertEquals(90f, result.getAssignmentScore());
        assertEquals(50, result.getAssignmentProgressPercentage());
        assertEquals(attemptValue, result.getAttemptNo());
    }

    @Test
    void getAssignmentScoreAndProgress_completed() {
        childCompletionValues.put(UUID.randomUUID(), 1f);

        when(progressService.findLatestActivity(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(deployedActivity.getActivityId())
                                        .setChildWalkableCompletionValues(childCompletionValues)));

        GradePassbackAssignment result =
                gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.completed, result.getAssignmentProgress());
        assertEquals(scoreValue.floatValue(), result.getAssignmentScore());
        assertEquals(100, result.getAssignmentProgressPercentage());
        assertEquals(attemptValue, result.getAttemptNo());
    }

    @Test
    void getAssignmentScoreAndProgress_multiPathway_inProgress() {
        childCompletionValues.put(UUID.randomUUID(), .2f);
        childCompletionValues.put(UUID.randomUUID(), .6f);

        when(progressService.findLatestActivity(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(deployedActivity.getActivityId())
                                        .setChildWalkableCompletionValues(childCompletionValues)));

        GradePassbackAssignment result =
                gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.inprogress, result.getAssignmentProgress());
        assertEquals(90f, result.getAssignmentScore());
        assertEquals(80, result.getAssignmentProgressPercentage());
        assertEquals(attemptValue, result.getAttemptNo());
    }

    @Test
    void getAssignmentScoreAndProgress_multiPathway_completed() {
        childCompletionValues.put(UUID.randomUUID(), .2f);
        childCompletionValues.put(UUID.randomUUID(), .6f);
        childCompletionValues.put(UUID.randomUUID(), .2f);

        when(progressService.findLatestActivity(deploymentId, deployedActivity.getActivityId(), userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(deployedActivity.getActivityId())
                                        .setChildWalkableCompletionValues(childCompletionValues)));

        GradePassbackAssignment result =
                gradePassbackService.getAssignmentScoreAndProgress(deploymentId, userId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.completed, result.getAssignmentProgress());
        assertEquals(scoreValue.floatValue(), result.getAssignmentScore());
        assertEquals(100, result.getAssignmentProgressPercentage());
        assertEquals(attemptValue, result.getAttemptNo());
    }

    @Test
    void getAssignmentItemScoreAndProgress_noDeploymentId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getAssignmentItemScoreAndProgress(null, userId, elementId, null));

        assertEquals("deploymentId is required", ex.getMessage());
    }

    @Test
    void getAssignmentItemScoreAndProgress_noUserId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getAssignmentItemScoreAndProgress(deploymentId, null, elementId, null));

        assertEquals("userId is required", ex.getMessage());
    }

    @Test
    void getAssignmentItemScoreAndProgress_noElementId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getAssignmentItemScoreAndProgress(deploymentId, userId, null, null));

        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void getAssignmentItemScoreAndProgress_notStarted() {
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.empty());
        when(studentScoreService.computeScore(deploymentId, userId, elementId, null))
                .thenReturn(Mono.just(new Score().setValue(0.0)));

        GradePassbackItem result =
                gradePassbackService.getAssignmentItemScoreAndProgress(deploymentId, userId, elementId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.not_started, result.getItemProgress());
        assertEquals(0f, result.getScore());
        assertEquals(0, result.getItemProgressPercentage());
    }

    @Test
    void getAssignmentItemScoreAndProgress_completed() {
        Completion completion = new Completion().setValue(1f);

        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.just(generalProgress.setCompletion(completion)));

        GradePassbackItem result =
                gradePassbackService.getAssignmentItemScoreAndProgress(deploymentId, userId, elementId, null).block();

        assertNotNull(result);

        assertEquals(GradePassbackProgressType.completed, result.getItemProgress());
        assertEquals(scoreValue.floatValue(), result.getScore());
        assertEquals(100, result.getItemProgressPercentage());
    }

    @Test
    void getGradePassbackAssignment_noDeploymentId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getGradePassbackAssignment(null, userId, elementId, ltiData));

        assertEquals("deploymentId is required", ex.getMessage());
    }

    @Test
    void getGradePassbackAssignment_noUsertId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getGradePassbackAssignment(deploymentId, null, elementId, ltiData));

        assertEquals("userId is required", ex.getMessage());
    }

    @Test
    void getGradePassbackAssignment_noElementId() {
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> gradePassbackService.getGradePassbackAssignment(deploymentId, userId, null, ltiData));

        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void getGradePassbackAssignment_notStarted() {
        when(attemptService.findLatestAttempt(deploymentId, rootElementId, userId))
                .thenReturn(Mono.empty());
        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.empty());
        when(studentScoreService.computeScore(eq(deploymentId), eq(userId), any(), eq(null)))
                .thenReturn(Mono.just(new Score().setValue(0.0)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.empty());

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(GradePassbackProgressType.not_started, result.getAssignmentProgress());
            assertEquals(0f, result.getAssignmentScore());
            assertEquals(0, result.getAssignmentProgressPercentage());
            assertEquals(0, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.not_started, gradePassbackItem.getItemProgress());
            assertEquals(0f, gradePassbackItem.getScore());
            assertEquals(0, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_inProgress() {
        Completion itemCompletion = new Completion().setValue(.8f);
        childCompletionValues.put(UUID.randomUUID(), .8f);

        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(rootElementId)
                        .setChildWalkableCompletionValues(childCompletionValues)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.just(generalProgress.setCompletion(itemCompletion)));

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(GradePassbackProgressType.inprogress, result.getAssignmentProgress());
            assertEquals(scoreValue.floatValue(), result.getAssignmentScore());
            assertEquals(80, result.getAssignmentProgressPercentage());
            assertEquals(1, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.completed, gradePassbackItem.getItemProgress());
            assertEquals(scoreValue.floatValue(), gradePassbackItem.getScore());
            assertEquals(100, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_completed() {
        Completion itemCompletion = new Completion().setValue(1f);
        childCompletionValues.put(UUID.randomUUID(), 1f);

        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(rootElementId)
                        .setChildWalkableCompletionValues(childCompletionValues)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.just(generalProgress.setCompletion(itemCompletion)));

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(GradePassbackProgressType.completed, result.getAssignmentProgress());
            assertEquals(scoreValue.floatValue(), result.getAssignmentScore());
            assertEquals(100, result.getAssignmentProgressPercentage());
            assertEquals(1, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.completed, gradePassbackItem.getItemProgress());
            assertEquals(scoreValue.floatValue(), gradePassbackItem.getScore());
            assertEquals(100, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_noDeploymentFound() {
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.empty());

        NotFoundFault ex = assertThrows(NotFoundFault.class,
                () -> gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block());

        assertTrue(ex.getMessage().contains("cannot find deployment by id: " + deploymentId));
    }

    @Test
    void getTotalGradePassbackActionCount_singleGradepassAction() {
        List<Action> actionList = Arrays.asList(sendFeedbackAction, changeScoreAction, gradePassbackAction1);

        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.just(actionList));

        Integer result = gradePassbackService.getTotalGradePassbackActionCount(deploymentId).block();

        assertEquals(1, result);
    }

    @Test
    void getTotalGradePassbackActionCount_noGradepassAction() {
        List<Action> actionList = Arrays.asList(sendFeedbackAction, changeScoreAction);

        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.just(actionList));

        Integer result = gradePassbackService.getTotalGradePassbackActionCount(deploymentId).block();

        assertEquals(0, result);
    }
    @Test
    void getTotalGradePassbackActionCount_gradepassActions_differentElementId() {
        List<Action> actionList = Arrays.asList(sendFeedbackAction, gradePassbackAction1, changeScoreAction, gradePassbackAction2);

        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.just(actionList));

        Integer result = gradePassbackService.getTotalGradePassbackActionCount(deploymentId).block();

        assertEquals(2, result);
    }

    @Test
    void getTotalGradePassbackActionCount_gradepassActions_sameElementId() {
        List<Action> actionList = Arrays.asList(sendFeedbackAction, gradePassbackAction1, gradePassbackAction1, gradePassbackAction1);

        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.just(actionList));

        Integer result = gradePassbackService.getTotalGradePassbackActionCount(deploymentId).block();

        assertEquals(1, result);
    }

    @Test
    void getGradePassbackAssignment_notStarted_gradeByCompletion() {
        List<Action> actionList = Arrays.asList(sendFeedbackAction, changeScoreAction, gradePassbackAction1);

        when(attemptService.findLatestAttempt(deploymentId, rootElementId, userId))
                .thenReturn(Mono.empty());
        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.empty());
        when(studentScoreService.computeScore(deploymentId, userId, rootElementId, null))
                .thenReturn(Mono.just(new Score().setValue(0.0)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.empty());
        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.empty());

        ltiData.setCustomGradingMethod("completion");

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(GradePassbackProgressType.not_started, result.getAssignmentProgress());
            assertEquals(0f, result.getAssignmentScore());
            assertEquals(0, result.getAssignmentProgressPercentage());
            assertEquals(0, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.not_started, gradePassbackItem.getItemProgress());
            assertEquals(0f, gradePassbackItem.getScore());
            assertEquals(0, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_completed_gradeByCompletion() {
        Completion itemCompletion = new Completion().setValue(1f);
        childCompletionValues.put(UUID.randomUUID(), 1f);
        List<Action> actionList = Arrays.asList(sendFeedbackAction, changeScoreAction, gradePassbackAction1, gradePassbackAction2);

        when(gradePassbackConfig.isCheckGradePassbackQuestionCount()).thenReturn(Boolean.TRUE);
        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(rootElementId)
                                        .setChildWalkableCompletionValues(childCompletionValues)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.just(generalProgress.setCompletion(itemCompletion)));
        when(learnerScenarioGateway.fetchAllById(deploymentId, changeId))
                .thenReturn(Flux.just(new LearnerScenario().setActions(actionList.toString())));
        when(actionDeserializer.reactiveDeserialize(any(String.class)))
                .thenReturn(Mono.just(actionList));
        when(deploymentGateway.findGradepassbackCount(deploymentId, changeId))
                .thenReturn(Mono.just(2l));

        ltiData.setCustomGradingMethod("completion");

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(GradePassbackProgressType.completed, result.getAssignmentProgress());
            assertEquals(100f, result.getAssignmentScore());
            assertEquals(100, result.getAssignmentProgressPercentage());
            assertEquals(1, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.completed, gradePassbackItem.getItemProgress());
            assertEquals(50f, gradePassbackItem.getScore());
            assertEquals(100, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_completed_noLTIData() {
        Completion itemCompletion = new Completion().setValue(1f);
        childCompletionValues.put(UUID.randomUUID(), 1f);

        when(progressService.findLatestActivity(deploymentId, rootElementId, userId))
                .thenReturn(Mono.just(activityProgress.setCoursewareElementId(rootElementId)
                                              .setChildWalkableCompletionValues(childCompletionValues)));
        when(progressService.findLatest(deploymentId, elementId, userId))
                .thenReturn(Mono.just(generalProgress.setCompletion(itemCompletion)));


        ltiData = null;

        cohortSummary.setType(EnrollmentType.INSTRUCTOR);
        when(cohortGateway.findCohortSummary(UUID.randomUUID())).thenReturn(Mono.just(cohortSummary));

        GradePassbackAssignment result =
                gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block();

        assertAll(() ->{
            assertNotNull(result);

            assertEquals(null, result.getUserId());
            assertEquals(null, result.getAssignmentId());
            assertEquals(null, result.getCourseId());


            assertEquals(GradePassbackProgressType.completed, result.getAssignmentProgress());
            assertEquals(scoreValue.floatValue(), result.getAssignmentScore());
            assertEquals(100, result.getAssignmentProgressPercentage());
            assertEquals(1, result.getAttemptNo());
            assertEquals(1, result.getItemScore().size());

            GradePassbackItem gradePassbackItem = result.getItemScore().get(0);

            assertEquals(GradePassbackProgressType.completed, gradePassbackItem.getItemProgress());
            assertEquals(scoreValue.floatValue(), gradePassbackItem.getScore());
            assertEquals(100, gradePassbackItem.getItemProgressPercentage());
        });
    }

    @Test
    void getGradePassbackAssignment_noDeployedActivityFound() {
        when(deploymentGateway.findLatest(deploymentId)).thenReturn(Mono.empty());

        NotFoundFault ex = assertThrows(NotFoundFault.class,
                                        () -> gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block());

        assertTrue(ex.getMessage().contains("cannot find cohort id"));
    }

    @Test
    void getGradePassbackAssignment_noCohortSummaryFound() {
        when(cohortGateway.findCohortSummary(cohortId)).thenReturn(Mono.empty());

        NotFoundFault ex = assertThrows(NotFoundFault.class,
                                        () -> gradePassbackService.getGradePassbackAssignment(deploymentId, userId, elementId, ltiData).block());

        assertTrue(ex.getMessage().contains("cannot find cohort summary"));
    }

    @Test
    void getGradePassbackActionCount_noGradepassAction() {
        when(deploymentGateway.findGradepassbackCount(deploymentId, changeId))
                .thenReturn(Mono.just(0l));
        Integer result = gradePassbackService.getGradePassbackActionCount(deploymentId).block();
        assertEquals(0, result);
    }

    @Test
    void getGradePassbackActionCount_singleGradepassAction() {
        when(deploymentGateway.findGradepassbackCount(deploymentId, changeId))
                .thenReturn(Mono.just(1l));
        Integer result = gradePassbackService.getGradePassbackActionCount(deploymentId).block();
        assertEquals(1, result);
    }

}