package com.smartsparrow.eval.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.learnerPathway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerScenarioService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.learner.service.StudentScopeService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ActivityProgressUpdateServiceTest {

    public static final UUID elementId = UUID.randomUUID();
    public static final UUID elementAncestryId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID changeId = UUID.randomUUID();
    public static final UUID deploymentId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID pathwayId = UUID.randomUUID();
    public static final UUID parentPathwayId = UUID.randomUUID();
    public static final UUID walkableId = UUID.randomUUID();
    private static final ChangeScoreAction scoreAction = new ChangeScoreAction();
    private static final String actions = "[" +
            "{" +
            "\"action\":\"CHANGE_PROGRESS\"," +
            "\"resolver\":{" +
            "\"type\":\"LITERAL\"" +
            "}," +
            "\"context\":{" +
            "\"progressionType\":\"INTERACTIVE_COMPLETE\"" +
            "}" +
            "}, {" +
            "\"action\": \"CHANGE_SCORE\"," +
            "\"resolver\": {" +
            "\"type\": \"LITERAL\"" +
            "}," +
            "\"context\": {" +
            "\"elementId\": \"f38a2a40-543c-11e9-9124-ffa2146f2d13\"," +
            "\"elementType\": \"INTERACTIVE\"," +
            "\"operator\": \"ADD\"," +
            "\"value\": 5.5" +
            "}" +
            "}, {" +
            "\"action\":\"GRADE\"," +
            "\"resolver\":{" +
            "\"type\":\"LITERAL\"" +
            "},\"context\":{}" +
            "}, {" +
            "\"action\":\"FUBAR\"," +
            "\"resolver\":{" +
            "\"type\":\"LITERAL\"" +
            "},\"context\":{}" +
            "}, {" +
            "\"action\":\"SEND_FEEDBACK\"," +
            "\"resolver\":{" +
            "\"type\":\"LITERAL\"" +
            "}," +
            "\"context\":{" +
            "\"value\":\"Well done mate!\"" +
            "}" +
            "}, {" +
            "\"action\":\"CHANGE_COMPETENCY\"," +
            "\"resolver\":{" +
            "\"type\":\"LITERAL\"" +
            "}," +
            "\"context\":{" +
            "\"documentId\":\"f38a2a40-543c-11e9-9124-ffa2146f2d13\"," +
            "\"documentItemId\":\"5eee65f6-3fd5-48cf-8db4-1acb287049cf\"," +
            "\"value\":1" +
            "}" +
            "}]";
    private final CoursewareElement element = new CoursewareElement()
            .setElementType(CoursewareElementType.ACTIVITY)
            .setElementId(elementId);
    private final CoursewareElement elementAncestry = new CoursewareElement()
            .setElementType(CoursewareElementType.INTERACTIVE)
            .setElementId(elementAncestryId);
    private final Attempt attempt = new Attempt()
            .setId(attemptId)
            .setStudentId(studentId)
            .setCoursewareElementId(elementId)
            .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
            .setDeploymentId(deploymentId)
            .setValue(1)
            .setParentId(pathwayId);
    private final Deployment deployment = new Deployment()
            .setId(deploymentId)
            .setChangeId(changeId);
    private final LearnerWalkable walkable = new LearnerInteractive()
            .setId(elementId)
            .setChangeId(changeId)
            .setDeploymentId(deploymentId);
    private final LearnerEvaluationRequest request = new LearnerEvaluationRequest()
            .setAttempt(attempt)
            .setDeployment(deployment)
            .setLearnerWalkable(walkable)
            .setParentPathwayId(parentPathwayId)
            .setStudentId(studentId);
    private final List<Progress> progresses = new ArrayList<>();
    private final List<CoursewareElement> elementList = new ArrayList<>();
    @InjectMocks
    private ActivityProgressUpdateService activityProgressUpdateService;
    @Mock
    private ProgressService progressService;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private AttemptService attemptService;
    @Mock
    private ProgressUpdateService activityImplementation;
    @Mock
    private Progress progress;
    @Mock
    private ProgressAction action;
    @Mock
    private LearnerEvaluationResponseContext responseContext;
    @Mock
    private LearnerEvaluationResponse evaluationResponse;
    @Mock
    private WalkableEvaluationResult walkableEvaluationResult;
    @Mock
    private Completion completion;
    @Mock
    private ProgressActionContext actionContext;
    @Mock
    private CoursewareHistoryService coursewareHistoryService;
    @Mock
    private LearnerScenarioService learnerScenarioService;
    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;
    @Mock
    private ActionDeserializer actionDeserializer;
    @Mock
    private EvaluationActionState evaluationActionState;
    @Mock
    private StudentScopeService studentScopeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progresses.add(progress);

        when(activityImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(
                LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(progress));
        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(new Attempt().setParentId(UUID.randomUUID())));
        when(progress.getAttemptId()).thenReturn(attemptId);
        when(progress.getCompletion()).thenReturn(completion);
        when(progress.getCompletion().getValue()).thenReturn(1f);
        when(responseContext.getProgresses()).thenReturn(progresses);
        when(evaluationResponse.getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
        when(evaluationResponse.getWalkableEvaluationResult().getId()).thenReturn(walkableId);
        responseContext.getProgresses().add(progress);
        when(progressService.findLatestActivity(deploymentId, elementId, studentId))
                .thenReturn(Mono.empty());
        when(learnerActivityService.findChildPathways(elementId, deploymentId))
                .thenReturn(Flux.just(learnerPathway()));
        when(responseContext.getResponse()).thenReturn(evaluationResponse);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(request);
        when(progressService.persist(any(ActivityProgress.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void updateProgress_rootLevelActivity() {
        elementList.add(element);
        responseContext.getAncestry().add(element);
        when(responseContext.getAncestry()).thenReturn(elementList);

        Progress response = activityProgressUpdateService.updateProgress(element, action, responseContext).block();
        ArgumentCaptor<ActivityProgress> persistCaptor = ArgumentCaptor.forClass(ActivityProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(deploymentId, response.getDeploymentId());
        assertEquals(elementId, response.getCoursewareElementId());
        assertEquals(walkableEvaluationResult.getId(), response.getEvaluationId());
    }

    @Test
    void updateProgress() {

        elementList.add(element);
        elementList.add(elementAncestry);
        responseContext.getAncestry().add(elementAncestry);
        when(responseContext.getAncestry()).thenReturn(elementList);
        when(actionDeserializer.deserialize("actions")).thenReturn(Lists.newArrayList(scoreAction));
        LearnerScenario scenarioOne = new LearnerScenario()
                .setId(UUID.randomUUID());
        LearnerScenario scenarioTwo = new LearnerScenario()
                .setId(UUID.randomUUID());
        when(learnerScenarioService.findAll(any(Deployment.class),
                                            any(UUID.class),
                                            eq(ScenarioLifecycle.ACTIVITY_COMPLETE)))
                .thenReturn(Flux.just(scenarioOne, scenarioTwo));
        when(scenarioEvaluationService.evaluateCondition(any(LearnerScenario.class),
                                                         any(EvaluationLearnerContext.class)))
                .thenReturn(Mono.just(new ScenarioEvaluationResult().setScenarioId(scenarioOne.getId()).setActions(
                        actions)));
        when(coursewareHistoryService.record(any(UUID.class),
                                             any(LearnerEvaluationRequest.class),
                                             any(CoursewareElement.class),
                                             any(Attempt.class),
                                             any(UUID.class)))
                .thenReturn(Mono.just(new CompletedWalkable()));

        Progress response = activityProgressUpdateService.updateProgress(element, action, responseContext).block();
        ArgumentCaptor<ActivityProgress> persistCaptor = ArgumentCaptor.forClass(ActivityProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(1f), persistCaptor.getValue().getCompletion().getValue());
        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(deploymentId, response.getDeploymentId());
        assertEquals(walkableEvaluationResult.getId(), response.getEvaluationId());
        assertEquals(elementId, response.getCoursewareElementId());
    }

    @Test
    void updateProgress_ACTIVITY_REPEAT() {
        elementList.add(element);
        elementList.add(elementAncestry);
        responseContext.getAncestry().add(elementAncestry);
        when(responseContext.getAncestry()).thenReturn(elementList);
        when(actionDeserializer.deserialize("actions")).thenReturn(Lists.newArrayList(scoreAction));
        LearnerScenario scenarioOne = new LearnerScenario()
                .setId(UUID.randomUUID());
        LearnerScenario scenarioTwo = new LearnerScenario()
                .setId(UUID.randomUUID());
        when(learnerScenarioService.findAll(any(Deployment.class),
                                            any(UUID.class),
                                            eq(ScenarioLifecycle.ACTIVITY_COMPLETE)))
                .thenReturn(Flux.just(scenarioOne, scenarioTwo));
        when(scenarioEvaluationService.evaluateCondition(any(LearnerScenario.class),
                                                         any(EvaluationLearnerContext.class)))
                .thenReturn(Mono.just(new ScenarioEvaluationResult().setScenarioId(scenarioOne.getId()).setActions(
                        actions)));
        when(coursewareHistoryService.record(any(UUID.class),
                                             any(LearnerEvaluationRequest.class),
                                             any(CoursewareElement.class),
                                             any(Attempt.class),
                                             any(UUID.class)))
                .thenReturn(Mono.just(new CompletedWalkable()));
        when(responseContext.getEvaluationActionState()).thenReturn(evaluationActionState);
        when(evaluationActionState.getProgressActionContext()).thenReturn(actionContext);
        when(actionContext.getProgressionType()).thenReturn(ProgressionType.ACTIVITY_REPEAT);
        when(responseContext.getEvaluationActionState().getCoursewareElement()).thenReturn(element);
        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(attempt));
        when(attemptService.newAttempt(any(UUID.class),
                                       any(UUID.class),
                                       any(CoursewareElementType.class),
                                       any(UUID.class),
                                       any(UUID.class),
                                       any(Integer.class)))
                .thenReturn(Mono.just(attempt));
        when(studentScopeService.resetScopesFor(any(UUID.class),
                                                any(UUID.class),
                                                any(UUID.class))).thenReturn(Flux.empty());


        Progress response = activityProgressUpdateService.updateProgress(element, action, responseContext).block();
        ArgumentCaptor<ActivityProgress> persistCaptor = ArgumentCaptor.forClass(ActivityProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(0f), persistCaptor.getValue().getCompletion().getValue());
        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(deploymentId, response.getDeploymentId());
        assertEquals(walkableEvaluationResult.getId(), response.getEvaluationId());
        assertEquals(elementId, response.getCoursewareElementId());
    }

}
