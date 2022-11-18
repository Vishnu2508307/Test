package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionContext;
import com.smartsparrow.eval.action.ActionResult;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressActionResult;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.action.scope.ChangeScopeAction;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.ActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeProgressActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeScopeActionConsumer;
import com.smartsparrow.eval.data.LearnerChangeScoreActionConsumer;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.LearnerGradePassbackActionConsumer;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.eval.service.ProgressUpdateService;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerActionConsumerServiceTest {

    @InjectMocks
    private LearnerActionConsumerService learnerActionConsumerService;

    @Mock
    private LearnerChangeProgressActionConsumer changeProgressActionConsumer;

    @Mock
    private LearnerChangeScopeActionConsumer learnerChangeScopeActionConsumer;

    @Mock
    private LearnerChangeScoreActionConsumer learnerChangeScoreActionConsumer;

    @Mock
    private LearnerGradePassbackActionConsumer gradePassbackActionConsumer;
    @Mock
    private Map<CoursewareElementType, Provider<ProgressUpdateService>> progressUpdateServiceProviders;

    @Mock
    private ProgressAction action;

    @Mock
    private ProgressActionResult actionResult;
    @Mock
    private EmptyActionResult emptyActionResult;

    @Mock
    private Progress interactiveProgress;
    @Mock
    private Progress parentPathwayProgress;
    @Mock
    private Progress parentActivityProgress;
    @Mock
    private ProgressUpdateService interactiveImplementation;
    @Mock
    private ProgressUpdateService activityImplementation;
    @Mock
    private ProgressUpdateService pathwayImplementation;
    @Mock
    private StudentProgressRTMProducer studentProgressRTMProducer;

    @Mock
    private LearnerEvaluationResponseContext learnerEvaluationResponseContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID parentActivityId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(progressUpdateServiceProviders.get(CoursewareElementType.INTERACTIVE))
                .thenReturn(Providers.of(interactiveImplementation));
        when(progressUpdateServiceProviders.get(CoursewareElementType.PATHWAY))
                .thenReturn(Providers.of(pathwayImplementation));
        when(progressUpdateServiceProviders.get(CoursewareElementType.ACTIVITY))
                .thenReturn(Providers.of(activityImplementation));

        when(interactiveImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(interactiveProgress));
        when(pathwayImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(parentPathwayProgress));
        when(activityImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(parentActivityProgress));
        TestPublisher<Exchange> publisher = TestPublisher.create();
        publisher.complete();

        when(interactiveProgress.getCoursewareElementId()).thenReturn(interactiveId);
        when(parentActivityProgress.getCoursewareElementId()).thenReturn(parentActivityId);
        when(parentPathwayProgress.getCoursewareElementId()).thenReturn(parentPathwayId);
        when(studentProgressRTMProducer.buildStudentProgressRTMConsumable(any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(Progress.class)))
                .thenReturn(studentProgressRTMProducer);

        Map<Action.Type, Provider<ActionConsumer<? extends Action<? extends ActionContext<?>>, ? extends ActionResult<?>>>> actionConsumers =
            new HashMap<>();
        actionConsumers.put(Action.Type.CHANGE_PROGRESS, Providers.of(changeProgressActionConsumer));
        actionConsumers.put(Action.Type.CHANGE_SCOPE, Providers.of(learnerChangeScopeActionConsumer));
        actionConsumers.put(Action.Type.CHANGE_SCORE, Providers.of(learnerChangeScoreActionConsumer));
        actionConsumers.put(Action.Type.GRADE_PASSBACK, Providers.of(gradePassbackActionConsumer));
        when(changeProgressActionConsumer.consume(any(ProgressAction.class), any(LearnerEvaluationResponseContext.class))).thenReturn(Mono.just(actionResult));
        when(learnerChangeScopeActionConsumer.consume(any(ChangeScopeAction.class), any(LearnerEvaluationResponseContext.class))).thenReturn(Mono.just(emptyActionResult));
        when(learnerChangeScoreActionConsumer.consume(any(ChangeScoreAction.class), any(LearnerEvaluationResponseContext.class))).thenReturn(Mono.just(emptyActionResult));
        when(gradePassbackActionConsumer.consume(any(GradePassbackAction.class), any(LearnerEvaluationResponseContext.class))).thenReturn(Mono.just(emptyActionResult));
        learnerActionConsumerService = new LearnerActionConsumerService(actionConsumers);
    }

    @Test
    void consumeGradePassbackAction() {
        LearnerEvaluationResponseContext context = buildLearnerEvaluationResponseContext();

        ActionResult actionResult = learnerActionConsumerService.consume(context.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                 stream().filter(a -> a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                 .collect(Collectors.toList()), context).blockLast();

        assertNotNull(actionResult);
    }

    @Test
    void consumeNonPassbackActions() {
        LearnerEvaluationResponseContext context = buildLearnerEvaluationResponseContext();

        ActionResult actionResult = learnerActionConsumerService.consume(context.getResponse().getWalkableEvaluationResult().getTriggeredActions().
                                                                                 stream().filter(a -> !a.getType().equals(Action.Type.GRADE_PASSBACK))
                                                                                 .collect(Collectors.toList()), context).blockLast();

        assertNotNull(actionResult);
    }


    private LearnerEvaluationResponseContext buildLearnerEvaluationResponseContext() {
        LearnerEvaluationResponseContext context = new LearnerEvaluationResponseContext();
        LearnerEvaluationResponse evaluationResponse = new LearnerEvaluationResponse();
        LearnerEvaluationRequest evaluationRequest = new LearnerEvaluationRequest();
        UUID studentId =  UUID.randomUUID();
        evaluationRequest.setStudentId(studentId);
        Attempt attempt = new Attempt();
        attempt.setParentId(UUID.randomUUID());
        attempt.setId(UUID.randomUUID());
        attempt.setDeploymentId(UUID.randomUUID());
        attempt.setCoursewareElementId(UUID.randomUUID());
        attempt.setCoursewareElementType(CoursewareElementType.INTERACTIVE);
        attempt.setStudentId(studentId);
        evaluationRequest.setAttempt(attempt);
        evaluationRequest.setParentPathwayId(UUID.randomUUID());
        evaluationRequest.setScenarioLifecycle(ScenarioLifecycle.INTERACTIVE_ENTRY);
        evaluationResponse.setEvaluationRequest(evaluationRequest);

        ScenarioEvaluationResult scenarioEvaluationResult = new ScenarioEvaluationResult();
        scenarioEvaluationResult.setEvaluationResult(true);
        scenarioEvaluationResult.setActions("[{\"action\":\"CHANGE_SCOPE\"," +
                                                    "\"resolver\":{\"type\":\"LITERAL\"}," +
                                                    "\"context\":{" +
                                                    "\"studentScopeURN\":\"6fa9a571-c1d0-11ec-8776-69d2ee6dcb42\"," +
                                                    "\"sourceId\":\"b32b9920-c1d0-11ec-8776-69d2ee6dcb42\"," +
                                                    "\"schemaProperty\":{" +
                                                    "\"label\":\"Learner input\"," +
                                                    "\"type\":\"text\",\"learnerEditable\":true" +
                                                    "}," +
                                                    "\"dataType\":\"STRING\"," +
                                                    "\"operator\":\"SET\"," +
                                                    "\"context\":[\"value\"]," +
                                                    "\"value\":\"test123456\"" +
                                                    "}" +
                                                    "}," +
                                                    "{\"action\":\"CHANGE_PROGRESS\"," +
                                                    "\"resolver\":{\"type\":\"LITERAL\"}," +
                                                    "\"context\":{" +
                                                    "\"progressionType\":\"INTERACTIVE_REPEAT\"" +
                                                    "}" +
                                                    "}," +
                                                    "{\"action\":\"SEND_FEEDBACK\"," +
                                                    "\"resolver\":{\"type\":\"LITERAL\"}," +
                                                    "\"context\":{\"value\":\"<p>you are correct!</p>\"" +
                                                    "}" +
                                                    "}]");
        scenarioEvaluationResult.setScenarioId(UUID.fromString("d13ae920-c1d0-11ec-8776-69d2ee6dcb42"));
        scenarioEvaluationResult.setScenarioCorrectness(ScenarioCorrectness.none);
        List<ScenarioEvaluationResult> scenarioEvaluationResults = new ArrayList<>();
        scenarioEvaluationResults.add(scenarioEvaluationResult);

        evaluationResponse.setScenarioEvaluationResults(scenarioEvaluationResults);

        WalkableEvaluationResult walkableEvaluationResult = new WalkableEvaluationResult();
        walkableEvaluationResult.setWalkableId(UUID.randomUUID());
        walkableEvaluationResult.setWalkableId(UUID.randomUUID());
        walkableEvaluationResult.setId(UUID.randomUUID());

        List<Action> triggeredActions = new ArrayList<>();
        ProgressActionContext progressActionContext = new ProgressActionContext();
        progressActionContext.setProgressionType(ProgressionType.INTERACTIVE_REPEAT);
        Action progressAction = new ProgressAction().setType(Action.Type.CHANGE_PROGRESS).setContext(progressActionContext);
        Action changeScoreAction = new ChangeScoreAction();
        Action gradePassbackAction = new GradePassbackAction();

        triggeredActions.add(progressAction);
        triggeredActions.add(changeScoreAction);
        triggeredActions.add(gradePassbackAction);
        walkableEvaluationResult.setTriggeredActions(triggeredActions);
        evaluationResponse.setWalkableEvaluationResult(walkableEvaluationResult);
        context.setResponse(evaluationResponse);
        context.setTimeId(UUID.randomUUID());

        return context;
    }
}
