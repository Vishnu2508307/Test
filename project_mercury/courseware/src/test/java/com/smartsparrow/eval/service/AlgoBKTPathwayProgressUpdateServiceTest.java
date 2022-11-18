package com.smartsparrow.eval.service;

import static com.smartsparrow.courseware.pathway.WalkableChildStub.buildWalkableChild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerBKTPathway;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AttemptService;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.learner.service.ProgressService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AlgoBKTPathwayProgressUpdateServiceTest {

    public static final UUID elementId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID evaluationId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID learnerPathwayId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final String changeProgressActionsIncomplete = "CHANGE_PROGRESS_INCOMPLETE";
    private static final UUID documentId = UUID.randomUUID();
    private static final UUID documentItemId = UUID.randomUUID();
    private static final UUID scenarioIdCorrect = UUID.randomUUID();
    private static final UUID documentVersionId = UUID.randomUUID();
    private static final WalkableChild walkableOne = buildWalkableChild();
    private static final WalkableChild walkableTwo = buildWalkableChild();
    private static final WalkableChild walkableThree = buildWalkableChild();
    private static final WalkableChild walkableFour = buildWalkableChild();
    private final List<Progress> progresses = new ArrayList<>();
    @InjectMocks
    private AlgoBKTPathwayProgressUpdateService algoBKTPathwayProgressUpdateService;
    @Mock
    private LearnerPathwayService learnerPathwayService;
    @Mock
    private ProgressService progressService;
    @Mock
    private AttemptService attemptService;
    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;
    @Mock
    private ProgressAction action;
    @Mock
    private LearnerEvaluationResponseContext context;
    @Mock
    private Progress progress;
    @Mock
    private WalkableEvaluationResult walkableEvaluationResult;
    @Mock
    private LearnerEvaluationResponse evaluationResponse;
    @Mock
    private LearnerWalkable learnerWalkable;
    @Mock
    private Completion completion;
    private LearnerBKTPathway learnerBKTPathway;
    private final Attempt attempt = new Attempt()
            .setId(attemptId)
            .setStudentId(studentId)
            .setCoursewareElementId(elementId)
            .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
            .setDeploymentId(deploymentId)
            .setValue(1)
            .setParentId(learnerPathwayId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progresses.add(progress);
        LearnerScenario correct = new LearnerScenario()
                .setId(scenarioIdCorrect)
                .setCorrectness(ScenarioCorrectness.incorrect);
        ScenarioEvaluationResult res1 = buildScenarioEvaluationResult(correct);
        learnerBKTPathway = new LearnerBKTPathway(progressService, learnerPathwayService);
        learnerBKTPathway
                .setId(pathwayId)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setConfig("{\"exitAfter\": 2," +
                                   "\"P_G\": 0.2," +
                                   "\"P_S\": 0.2," +
                                   "\"P_T\": 0.9," +
                                   "\"P_L0\":0.4," +
                                   "\"P_LN\":0.9," +
                                   "\"competency\": [{" +
                                   "\"documentId\": \"" + documentId + "\"," +
                                   "\"documentItemId\": \"" + documentItemId + "\"" +
                                   "}]," +
                                   "\"maintainFor\": 3 }");

        LearnerEvaluationRequest learnerEvaluationRequest = new LearnerEvaluationRequest()
                .setStudentId(studentId)
                .setLearnerWalkable(learnerWalkable);

        when(context.getResponse()).thenReturn(evaluationResponse);
        when(context.getResponse().getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
        when(evaluationResponse.getWalkableEvaluationResult()).thenReturn(walkableEvaluationResult);
        when(evaluationResponse.getWalkableEvaluationResult().getId()).thenReturn(evaluationId);
        when(context.getProgresses()).thenReturn(progresses);
        when(evaluationResponse.getEvaluationRequest()).thenReturn(learnerEvaluationRequest);
        when(progressService.findLatestNBKTPathway(deploymentId,
                                                   pathwayId,
                                                   studentId,
                                                   1)).thenReturn(Flux.just(new BKTPathwayProgress()));
        when(attemptService.findById(context.getProgresses().get(0).getAttemptId())).thenReturn(Mono.just(attempt));
        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree,
                walkableFour
        ));
        when(progress.getCompletion()).thenReturn(completion);
        when(progress.getCompletion().getValue()).thenReturn(1f);
        when(progressService.persist(any(BKTPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));
        when(walkableEvaluationResult.getTruthfulScenario()).thenReturn(res1);
        when(progressService.findLatestNBKTPathway(deploymentId,
                                                   pathwayId,
                                                   studentId,
                                                   learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.empty());
        when(learnerCompetencyDocumentService.findDocument(documentId))
                .thenReturn(Mono.just(new LearnerDocument()
                                              .setDocumentVersionId(documentVersionId)));
    }

    @Test
    void updateProgress() {
        Progress response = algoBKTPathwayProgressUpdateService.updateProgress(learnerBKTPathway,
                                                                               action,
                                                                               context).block();
        ArgumentCaptor<BKTPathwayProgress> persistCaptor = ArgumentCaptor.forClass(BKTPathwayProgress.class);
        verify(progressService).persist(persistCaptor.capture());
        assertEquals(Float.valueOf(0.5f), persistCaptor.getValue().getCompletion().getValue());
        assertNotNull(response);
        assertEquals(studentId, response.getStudentId());
        assertEquals(deploymentId, response.getDeploymentId());
        assertEquals(evaluationId, response.getEvaluationId());
    }

    private ScenarioEvaluationResult buildScenarioEvaluationResult(Scenario scenario) {
        return new ScenarioEvaluationResult()
                .setActions(AlgoBKTPathwayProgressUpdateServiceTest.changeProgressActionsIncomplete)
                .setEvaluationResult(true)
                .setScenarioCorrectness(scenario.getCorrectness())
                .setScenarioId(scenario.getId());
    }
}
