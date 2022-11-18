package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.service.LearnerEvaluationResponseEnricher;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerWalkableServiceTest {

    @InjectMocks
    private LearnerWalkableService learnerWalkableService;

    @Mock
    private LearnerActivityService learnerActivityService;

    @Mock
    private LearnerInteractiveService learnerInteractiveService;

    @Mock
    private AcquireAttemptService acquireAttemptService;

    @Mock
    private EvaluationSubmitService evaluationSubmitService;

    @Mock
    private LearnerEvaluationResponseEnricher learnerEvaluationResponseEnricher;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private LearnerActionConsumerService learnerActionConsumerService;

    @Mock
    private EvaluationResultService evaluationResultService;

    @Mock
    private CacheService cacheService;

    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID walkableId = UUIDs.timeBased();
    private static final UUID changeId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID timeId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(learnerEvaluationResponseEnricher.enrich(any(LearnerEvaluationResponseContext.class)))
                .thenAnswer((Answer<Mono<LearnerEvaluationResponseContext>>) invocation -> Mono.just(invocation.getArgument(0)));
        when(learnerActionConsumerService.consume(anyList(), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Flux.just(new EmptyActionResult(new ChangeScoreAction())));
        when(learnerActionConsumerService.consume(anyList(), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Flux.just(new EmptyActionResult(new GradePassbackAction())));
        when(evaluationResultService.persist(any(Evaluation.class)))
                .thenAnswer((Answer<Mono<Evaluation>>) invocation -> Mono.just(invocation.getArgument(0)));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(studentId));
        Map<String, String> ltiParams = new HashMap<>();
        ltiParams.put("user_id", "1232424");
        ltiParams.put("custom_platform_assignment_id", "8765532");
        ltiParams.put("custom_sms_course_id", "6767676");
        ltiParams.put("custom_attempt_limit", "1");
        ltiParams.put("custom_grading_method", "accuracy");

        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", authenticationContext.getAccount().getId(), deploymentId);

        when(cacheService.computeIfAbsent(cacheName, (Class<Map<String, String>>) (Class<?>) Map.class, Mono.empty())).thenReturn(Mono.just(ltiParams));
    }

    @Test
    void findLearnerWalkable_nullDeploymentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findLearnerWalkable(null, null, null));

        assertEquals("deploymentId is required", f.getMessage());
    }

    @Test
    void findLearnerWalkable_nullWalkableId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findLearnerWalkable(deploymentId, null, null));

        assertEquals("walkableId is required", f.getMessage());
    }

    @Test
    void findLearnerWalkable_nullWalkableType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findLearnerWalkable(deploymentId, walkableId, null));

        assertEquals("walkableType is required", f.getMessage());
    }

    @Test
    void findLearnerWalkable_invalidWalkableType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(one -> !CoursewareElementType.isAWalkable(one))
                .forEach(elementType -> {
                    IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                            () -> learnerWalkableService.findLearnerWalkable(deploymentId, walkableId, elementType));

                    assertEquals(String.format("%s not a walkableType", elementType), f.getMessage());
                });
    }

    @Test
    void findLearnerWalkable_activity() {
        when(learnerActivityService.findActivity(walkableId, deploymentId))
                .thenReturn(Mono.just(new LearnerActivity()
                        .setEvaluationMode(EvaluationMode.DEFAULT)));

        LearnerWalkable found = learnerWalkableService.findLearnerWalkable(deploymentId, walkableId, CoursewareElementType.ACTIVITY)
                .block();

        assertNotNull(found);
        assertEquals(EvaluationMode.DEFAULT, found.getEvaluationMode());

        verify(learnerActivityService).findActivity(walkableId, deploymentId);
        verify(learnerInteractiveService, never()).findInteractive(any(UUID.class), any(UUID.class));
    }


    @Test
    void findLearnerWalkable_interactive() {
        when(learnerInteractiveService.findInteractive(walkableId, deploymentId))
                .thenReturn(Mono.just(new LearnerInteractive()
                        .setEvaluationMode(EvaluationMode.DEFAULT)));

        LearnerWalkable found = learnerWalkableService.findLearnerWalkable(deploymentId, walkableId, CoursewareElementType.INTERACTIVE)
                .block();

        assertNotNull(found);
        assertEquals(EvaluationMode.DEFAULT, found.getEvaluationMode());

        verify(learnerInteractiveService).findInteractive(walkableId, deploymentId);
        verify(learnerActivityService, never()).findActivity(any(UUID.class), any(UUID.class));
    }


    @Test
    void findParentPathwayId_nullDeploymentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findParentPathwayId(null, null, null));

        assertEquals("deploymentId is required", f.getMessage());
    }

    @Test
    void findParentPathwayId_nullWalkableId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findParentPathwayId(deploymentId, null, null));

        assertEquals("walkableId is required", f.getMessage());
    }

    @Test
    void findParentPathway_nullWalkableId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.findParentPathwayId(deploymentId, walkableId, null));

        assertEquals("walkableType is required", f.getMessage());
    }

    @Test
    void findParentPathway_invalidWalkableType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(one -> !CoursewareElementType.isAWalkable(one))
                .forEach(elementType -> {
                    IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                            () -> learnerWalkableService.findParentPathwayId(deploymentId, walkableId, elementType));

                    assertEquals(String.format("%s not a walkableType", elementType), f.getMessage());
                });
    }

    @Test
    void findParentPathway_activity() {
        final UUID pathwayId = UUIDs.timeBased();
        when(learnerActivityService.findParentPathwayId(walkableId, deploymentId))
                .thenReturn(Mono.just(pathwayId));

        final UUID found = learnerWalkableService.findParentPathwayId(deploymentId, walkableId, CoursewareElementType.ACTIVITY)
                .block();

        assertNotNull(found);
        assertEquals(pathwayId, found);

        verify(learnerActivityService).findParentPathwayId(walkableId, deploymentId);
        verify(learnerInteractiveService, never()).findParentPathwayId(walkableId, deploymentId);
    }

    @Test
    void findParentPathway_interactive() {
        final UUID pathwayId = UUIDs.timeBased();
        when(learnerInteractiveService.findParentPathwayId(walkableId, deploymentId))
                .thenReturn(Mono.just(pathwayId));

        final UUID found = learnerWalkableService.findParentPathwayId(deploymentId, walkableId, CoursewareElementType.INTERACTIVE)
                .block();

        assertNotNull(found);
        assertEquals(pathwayId, found);

        verify(learnerInteractiveService).findParentPathwayId(walkableId, deploymentId);
        verify(learnerActivityService, never()).findParentPathwayId(walkableId, deploymentId);
    }

    @Test
    void evaluate_nullDeploymentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.evaluate(null, null, null, null, null));

        assertEquals("deploymentId is required", f.getMessage());
    }

    @Test
    void evaluate_nullWalkableId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.evaluate(deploymentId, null, null, null, null));

        assertEquals("walkableId is required", f.getMessage());
    }

    @Test
    void evaluate_nullWalkableType() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.evaluate(deploymentId, walkableId, null, null, null));

        assertEquals("walkableType is required", f.getMessage());
    }

    @Test
    void evaluate_nullStudentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.evaluate(deploymentId, walkableId, CoursewareElementType.ACTIVITY, null, null));

        assertEquals("studentId is required", f.getMessage());
    }

    @Test
    void evaluate_nullProducingClientId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerWalkableService.evaluate(deploymentId, walkableId, CoursewareElementType.ACTIVITY, studentId, null));

        assertEquals("producingClientId is required", f.getMessage());
    }

    @Test
    void evaluate_invalidWalkableType() {
        Arrays.stream(CoursewareElementType.values())
                .filter(one -> !CoursewareElementType.isAWalkable(one))
                .forEach(elementType -> {
                    IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                            () -> learnerWalkableService.evaluate(deploymentId, walkableId, elementType, studentId, "clientId"));

                    assertEquals("elementType must be a walkable type", f.getMessage());
                });
    }

    @Test
    void evaluate_interactive_nullParentPathway() {
        final LearnerInteractive interactive = new LearnerInteractive()
                .setChangeId(changeId)
                .setId(walkableId)
                .setDeploymentId(deploymentId)
                .setEvaluationMode(EvaluationMode.COMBINED);

        final Attempt attempt = new Attempt()
                .setId(UUIDs.timeBased())
                .setDeploymentId(deploymentId)
                .setStudentId(studentId)
                .setValue(1)
                .setCoursewareElementId(walkableId)
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE);

        final DeployedActivity deployedActivity = new DeployedActivity()
                .setCohortId(UUIDs.timeBased())
                .setActivityId(UUIDs.timeBased())
                .setId(deploymentId)
                .setChangeId(changeId);

        ArgumentCaptor<LearnerEvaluationRequest> requestArgumentCaptor = ArgumentCaptor.forClass(LearnerEvaluationRequest.class);

        when(learnerInteractiveService.findParentPathwayId(walkableId, deploymentId))
                .thenReturn(Mono.just(parentPathwayId));
        when(learnerInteractiveService.findInteractive(walkableId, deploymentId))
                .thenReturn(Mono.just(interactive));
        when(acquireAttemptService.acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId))
                .thenReturn(Mono.just(attempt));
        when(evaluationSubmitService.submit(any(LearnerEvaluationRequest.class), eq(LearnerEvaluationResponse.class)))
                .thenReturn(Mono.just(new LearnerEvaluationResponse()
                        .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                .setTruthfulScenario(new ScenarioEvaluationResult()))
                        .setScenarioEvaluationResults(new ArrayList<>())
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setAttempt(attempt)
                                .setLearnerWalkable(interactive))));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployedActivity));

        final LearnerEvaluationResponse response = learnerWalkableService
                .evaluate(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId, "clientId")
                .block();

        assertNotNull(response);

        verify(evaluationSubmitService).submit(requestArgumentCaptor.capture(), eq(LearnerEvaluationResponse.class));
        verify(acquireAttemptService).acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId);

        final LearnerEvaluationRequest request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(interactive, request.getLearnerWalkable());
        assertEquals(attempt, request.getAttempt());
        assertEquals(parentPathwayId, request.getParentPathwayId());
        assertEquals(studentId, request.getStudentId());
        assertEquals("clientId", request.getProducingClientId());
        assertEquals(ScenarioLifecycle.INTERACTIVE_EVALUATE, request.getScenarioLifecycle());
        assertEquals(deployedActivity, request.getDeployment());
        assertEquals(2, response.getWalkableEvaluationResult().getActionResults().size());

        ArgumentCaptor<LearnerEvaluationResponseContext> contextCaptor = ArgumentCaptor.forClass(LearnerEvaluationResponseContext.class);
        verify(learnerEvaluationResponseEnricher).enrich(contextCaptor.capture());
        final LearnerEvaluationResponseContext context = contextCaptor.getValue();
        assertNotNull(context);
        assertEquals(response, context.getResponse());
    }

    @Test
    void evaluate_interactive() {
        final LearnerInteractive interactive = new LearnerInteractive()
                .setChangeId(changeId)
                .setId(walkableId)
                .setDeploymentId(deploymentId)
                .setEvaluationMode(EvaluationMode.COMBINED);

        final Attempt attempt = new Attempt()
                .setId(UUIDs.timeBased())
                .setDeploymentId(deploymentId)
                .setStudentId(studentId)
                .setValue(1)
                .setCoursewareElementId(walkableId)
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE);

        final DeployedActivity deployedActivity = new DeployedActivity()
                .setCohortId(UUIDs.timeBased())
                .setActivityId(UUIDs.timeBased())
                .setId(deploymentId)
                .setChangeId(changeId);

        ArgumentCaptor<LearnerEvaluationRequest> requestArgumentCaptor = ArgumentCaptor.forClass(LearnerEvaluationRequest.class);

        when(learnerInteractiveService.findInteractive(walkableId, deploymentId))
                .thenReturn(Mono.just(interactive));
        when(acquireAttemptService.acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId))
                .thenReturn(Mono.just(attempt));
        when(evaluationSubmitService.submit(any(LearnerEvaluationRequest.class), eq(LearnerEvaluationResponse.class)))
                .thenReturn(Mono.just(new LearnerEvaluationResponse()
                        .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                .setTruthfulScenario(new ScenarioEvaluationResult()))
                        .setScenarioEvaluationResults(new ArrayList<>())
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setAttempt(attempt)
                                .setLearnerWalkable(interactive))));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployedActivity));

        when(learnerWalkableService.findParentPathwayId(deploymentId, walkableId, CoursewareElementType.INTERACTIVE)).thenReturn(Mono.just(parentPathwayId));
        final LearnerEvaluationResponse response = learnerWalkableService
                .evaluate(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId, "clientId")
                .block();

        assertNotNull(response);
        assertEquals(2, response.getWalkableEvaluationResult().getActionResults().size());

        verify(evaluationSubmitService).submit(requestArgumentCaptor.capture(), eq(LearnerEvaluationResponse.class));
        verify(acquireAttemptService).acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId);
        verify(deploymentService).findDeployment(deploymentId);

        final LearnerEvaluationRequest request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(interactive, request.getLearnerWalkable());
        assertEquals(attempt, request.getAttempt());
        assertEquals(parentPathwayId, request.getParentPathwayId());
        assertEquals(studentId, request.getStudentId());
        assertEquals("clientId", request.getProducingClientId());
        assertEquals(ScenarioLifecycle.INTERACTIVE_EVALUATE, request.getScenarioLifecycle());
        assertEquals(deployedActivity, request.getDeployment());

        ArgumentCaptor<LearnerEvaluationResponseContext> contextCaptor = ArgumentCaptor.forClass(LearnerEvaluationResponseContext.class);
        verify(learnerEvaluationResponseEnricher).enrich(contextCaptor.capture());
        final LearnerEvaluationResponseContext context = contextCaptor.getValue();
        assertNotNull(context);
        assertEquals(response, context.getResponse());
        ArgumentCaptor<Evaluation> evaluationCaptor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationResultService).persist(evaluationCaptor.capture());
        final Evaluation evaluation = evaluationCaptor.getValue();
        assertNotNull(evaluation);

    }

    @Test
    void evaluate_interactive_timeId() {
        final LearnerInteractive interactive = new LearnerInteractive()
                .setChangeId(changeId)
                .setId(walkableId)
                .setDeploymentId(deploymentId)
                .setEvaluationMode(EvaluationMode.COMBINED);

        final Attempt attempt = new Attempt()
                .setId(UUIDs.timeBased())
                .setDeploymentId(deploymentId)
                .setStudentId(studentId)
                .setValue(1)
                .setCoursewareElementId(walkableId)
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE);

        final DeployedActivity deployedActivity = new DeployedActivity()
                .setCohortId(UUIDs.timeBased())
                .setActivityId(UUIDs.timeBased())
                .setId(deploymentId)
                .setChangeId(changeId);

        ArgumentCaptor<LearnerEvaluationRequest> requestArgumentCaptor = ArgumentCaptor.forClass(LearnerEvaluationRequest.class);
        when(learnerActionConsumerService.consume(anyList(), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Flux.just(new EmptyActionResult(new GradePassbackAction())));
        when(learnerInteractiveService.findInteractive(walkableId, deploymentId))
                .thenReturn(Mono.just(interactive));
        when(acquireAttemptService.acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId))
                .thenReturn(Mono.just(attempt));
        when(evaluationSubmitService.submit(any(LearnerEvaluationRequest.class), eq(LearnerEvaluationResponse.class)))
                .thenReturn(Mono.just(new LearnerEvaluationResponse()
                                              .setWalkableEvaluationResult(new WalkableEvaluationResult()
                                                                                   .setTruthfulScenario(new ScenarioEvaluationResult()))
                                              .setScenarioEvaluationResults(new ArrayList<>())
                                              .setEvaluationRequest(new LearnerEvaluationRequest()
                                                                            .setAttempt(attempt)
                                                                            .setLearnerWalkable(interactive))));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(deployedActivity));

        when(learnerWalkableService.findParentPathwayId(deploymentId, walkableId, CoursewareElementType.INTERACTIVE)).thenReturn(Mono.just(parentPathwayId));
        final LearnerEvaluationResponse response = learnerWalkableService
                .evaluate(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId, "clientId", timeId)
                .block();

        assertNotNull(response);
        assertEquals(2, response.getWalkableEvaluationResult().getActionResults().size());

        verify(evaluationSubmitService).submit(requestArgumentCaptor.capture(), eq(LearnerEvaluationResponse.class));
        verify(acquireAttemptService).acquireLatestAttempt(deploymentId, walkableId, CoursewareElementType.INTERACTIVE, studentId);
        verify(deploymentService).findDeployment(deploymentId);

        final LearnerEvaluationRequest request = requestArgumentCaptor.getValue();

        assertNotNull(request);
        assertEquals(interactive, request.getLearnerWalkable());
        assertEquals(attempt, request.getAttempt());
        assertEquals(parentPathwayId, request.getParentPathwayId());
        assertEquals(studentId, request.getStudentId());
        assertEquals("clientId", request.getProducingClientId());
        assertEquals(ScenarioLifecycle.INTERACTIVE_EVALUATE, request.getScenarioLifecycle());
        assertEquals(deployedActivity, request.getDeployment());

        ArgumentCaptor<LearnerEvaluationResponseContext> contextCaptor = ArgumentCaptor.forClass(LearnerEvaluationResponseContext.class);
        verify(learnerEvaluationResponseEnricher).enrich(contextCaptor.capture());
        final LearnerEvaluationResponseContext context = contextCaptor.getValue();
        assertNotNull(context);
        assertEquals(response, context.getResponse());
        ArgumentCaptor<Evaluation> evaluationCaptor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationResultService).persist(evaluationCaptor.capture());
        final Evaluation evaluation = evaluationCaptor.getValue();
        assertNotNull(evaluation);

    }
}
