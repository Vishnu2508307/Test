package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.attemptId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.deploymentId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.learnerPathway;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.progressEventCompleted;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.studentId;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.service.ScenarioEvaluationService;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateActivityProgressHandlerTest {


    @InjectMocks
    private UpdateActivityProgressHandler handler;

    private UpdateActivityProgressHandler spy;

    @Mock
    private LearnerActivityService learnerActivityService;

    @Mock
    private ProgressService progressService;

    @Mock
    private AttemptService attemptService;

    @Mock
    private LearnerScenarioService learnerScenarioService;

    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;

    @Mock
    private CoursewareHistoryService coursewareHistoryService;
    @Mock
    private StudentProgressRTMProducer studentProgressRTMProducer;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        spy = mockProgressHandler(handler);

        when(attemptService.findById(eq(attemptId))).thenReturn(Mono.just(new Attempt().setParentId(UUID.randomUUID())));
        when(progressService.persist(any(ActivityProgress.class))).thenReturn(Flux.just(new Void[]{}));
        LearnerScenario scenarioOne = new LearnerScenario()
                .setId(UUID.randomUUID());
        LearnerScenario scenarioTwo = new LearnerScenario()
                .setId(UUID.randomUUID());

        when(learnerScenarioService.findAll(any(Deployment.class), any(UUID.class), eq(ScenarioLifecycle.ACTIVITY_COMPLETE)))
                .thenReturn(Flux.just(scenarioOne, scenarioTwo));

        when(scenarioEvaluationService.evaluateCondition(any(LearnerScenario.class), any(EvaluationLearnerContext.class)))
                .thenReturn(Mono.just(new ScenarioEvaluationResult().setScenarioId(scenarioOne.getId()).setActions(actions)));

        when(coursewareHistoryService.record(any(UUID.class), any(EvaluationResult.class), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(new CompletedWalkable()));
        when(studentProgressRTMProducer.buildStudentProgressRTMConsumable(any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(Progress.class)))
                .thenReturn(studentProgressRTMProducer);
    }

    @Test
    void updateProgress_nonRootElement() {
        UUID parentId = UUID.randomUUID();

        CoursewareElement parent = new CoursewareElement()
                .setElementId(parentId)
                .setElementType(CoursewareElementType.PATHWAY);

        when(progressService.findLatestActivity(deploymentId, elementId, studentId))
                .thenReturn(Mono.empty());

        when(learnerActivityService.findChildPathways(elementId, deploymentId))
                .thenReturn(Flux.just(learnerPathway()));

        UpdateCoursewareElementProgressEvent event = progressEventCompleted(parent, CoursewareElementType.ACTIVITY);
        updateActivityProgressCommonTest(event);
    }

    @Test
    void updateProgress_rootElement() {

        when(progressService.findLatestActivity(deploymentId, elementId, studentId))
                .thenReturn(Mono.empty());

        when(learnerActivityService.findChildPathways(elementId, deploymentId))
                .thenReturn(Flux.just(learnerPathway()));

        UpdateCoursewareElementProgressEvent event = progressEventCompleted(null, CoursewareElementType.ACTIVITY);
        updateActivityProgressCommonTest(event);

    }

    private void updateActivityProgressCommonTest(UpdateCoursewareElementProgressEvent event) {

        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.ACTIVITY_COMPLETE)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.ACTIVITY);

        Exchange exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, progressActionContext);

        spy.updateProgress(exchange);

        ArgumentCaptor<ActivityProgress> persistCaptor = ArgumentCaptor.forClass(ActivityProgress.class);
        ArgumentCaptor<ActivityProgress> broadcastCaptor = ArgumentCaptor.forClass(ActivityProgress.class);
        ArgumentCaptor<ActivityProgress> propagateCaptor = ArgumentCaptor.forClass(ActivityProgress.class);

        verify(progressService).persist(persistCaptor.capture());

        verify(spy, times(1)).propagateProgressChangeUpwards(
                eq(exchange),
                eq(event),
                propagateCaptor.capture());

        verify(spy, times(1)).broadcastProgressEventMessage(
                broadcastCaptor.capture(),
                eq(event.getUpdateProgressEvent()));

        ActivityProgress capturedPersist = persistCaptor.getValue();
        ActivityProgress broadcastPersist = broadcastCaptor.getValue();
        ActivityProgress propagatePersist = propagateCaptor.getValue();
        assertEquals(capturedPersist, broadcastPersist);
        assertEquals(capturedPersist, propagatePersist);

        assertEquals(deploymentId, capturedPersist.getDeploymentId());
        assertEquals(elementId, capturedPersist.getCoursewareElementId());
        assertEquals(studentId, capturedPersist.getStudentId());

        assertNotNull(capturedPersist);
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getValue());
        assertEquals(Float.valueOf(1.0f), capturedPersist.getCompletion().getConfidence());
        verify(coursewareHistoryService).record(eq(studentId), any(EvaluationResult.class), eq(CoursewareElementType.ACTIVITY));
    }

}
