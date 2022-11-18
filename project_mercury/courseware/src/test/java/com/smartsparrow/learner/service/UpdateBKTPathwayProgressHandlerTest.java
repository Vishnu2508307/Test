package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.pathway.WalkableChildStub.buildWalkableChild;
import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.BKTPathway;
import com.smartsparrow.courseware.pathway.LearnerBKTPathway;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.event.UpdateProgressMessage;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateBKTPathwayProgressHandlerTest {

    @InjectMocks
    private UpdateBKTPathwayProgressHandler updateBKTPathwayProgressHandler;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ProgressService progressService;

    @Mock
    private AttemptService attemptService;

    @Mock
    private CompetencyMetService competencyMetService;

    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Mock
    private Exchange exchange;
    @Mock
    private UpdateCoursewareElementProgressEvent event;
    @Mock
    private UpdateProgressMessage updateProgressMessage;
    @Mock
    private Progress childProgress;
    @Mock
    private LearnerBKTPathway learnerBKTPathway;
    @Mock
    private Attempt childAttempt;
    @Mock
    private EvaluationEventMessage eventMessage;
    @Mock
    private EvaluationResult evaluationResult;
    @Mock
    private BKTPathway.ConfiguredDocumentItem configuredDocumentItem;
    @Mock
    private CompetencyMet competencyMet;

    private UpdateBKTPathwayProgressHandler spy;
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID evaluationId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID childAttemptId = UUID.randomUUID();
    private static final UUID pathwayAttemptId = UUID.randomUUID();
    private static final UUID documentId = UUID.randomUUID();
    private static final UUID documentItemId = UUID.randomUUID();
    private static final List<Progress> progresses = new ArrayList<>();
    private static WalkableChild walkableOne = buildWalkableChild();
    private static WalkableChild walkableTwo = buildWalkableChild();
    private static WalkableChild walkableThree = buildWalkableChild();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        learnerBKTPathway = mock(LearnerBKTPathway.class);

        spy = mockProgressHandler(updateBKTPathwayProgressHandler);

        when(configuredDocumentItem.getDocumentId()).thenReturn(documentId);
        when(configuredDocumentItem.getDocumentItemId()).thenReturn(documentItemId);

        when(learnerBKTPathway.getId()).thenReturn(pathwayId);
        when(learnerBKTPathway.getDeploymentId()).thenReturn(deploymentId);
        when(learnerBKTPathway.getChangeId()).thenReturn(changeId);
        when(learnerBKTPathway.getConfig()).thenReturn("{\"exitAfter\": 2}");
        when(learnerBKTPathway.getExitAfter()).thenReturn(3);
        when(learnerBKTPathway.getL0()).thenReturn(0.5);
        when(learnerBKTPathway.getPLN()).thenReturn(0.85);
        when(learnerBKTPathway.getGuessProbability()).thenReturn(0.2);
        when(learnerBKTPathway.getSlipProbability()).thenReturn(0.2);
        when(learnerBKTPathway.getTransitProbability()).thenReturn(0.3);
        when(learnerBKTPathway.getMaintainFor()).thenReturn(2);
        when(learnerBKTPathway.getCompetency()).thenReturn(Lists.newArrayList(configuredDocumentItem));

        Message message = mock(Message.class);

        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(UpdateCoursewareElementProgressEvent.class)).thenReturn(event);
        when(childProgress.getAttemptId()).thenReturn(childAttemptId);
        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);

        progresses.add(childProgress);

        when(event.getElement()).thenReturn(CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY));
        when(event.getUpdateProgressEvent()).thenReturn(updateProgressMessage);
        when(updateProgressMessage.getChangeId()).thenReturn(changeId);
        when(updateProgressMessage.getDeploymentId()).thenReturn(deploymentId);
        when(updateProgressMessage.getEvaluationId()).thenReturn(evaluationId);
        when(updateProgressMessage.getStudentId()).thenReturn(studentId);
        when(event.getEventProgress()).thenReturn(progresses);

        when(eventMessage.getDeploymentId()).thenReturn(deploymentId);
        when(eventMessage.getChangeId()).thenReturn(changeId);
        when(eventMessage.getAttemptId()).thenReturn(pathwayAttemptId);
        when(eventMessage.getStudentId()).thenReturn(studentId);
        when(eventMessage.getEvaluationResult()).thenReturn(evaluationResult);

        when(evaluationResult.getId()).thenReturn(evaluationId);
        when(evaluationResult.getInteractiveComplete()).thenReturn(true);
        when(evaluationResult.getScenarioCorrectness()).thenReturn(ScenarioCorrectness.correct);

        when(childAttempt.getParentId()).thenReturn(pathwayAttemptId);

        when(attemptService.findById(childAttemptId)).thenReturn(Mono.just(childAttempt));

        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree
        ));

        when(progressService.persist(any(BKTPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));

        when(learnerPathwayService.find(pathwayId, deploymentId, LearnerBKTPathway.class))
                .thenReturn(Mono.just(learnerBKTPathway));
        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.empty());

        when(competencyMetService.create(eq(studentId), eq(deploymentId), eq(changeId), eq(pathwayId), eq(CoursewareElementType.PATHWAY),
                eq(evaluationId), eq(documentId), any(UUID.class), eq(documentItemId), eq(pathwayAttemptId), any(Float.class), eq(1f)))
                .thenReturn(Mono.just(competencyMet));

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.empty());

        when(learnerCompetencyDocumentService.findDocument(documentId)).thenReturn(Mono.just(new LearnerDocument()
                .setDocumentVersionId(UUID.randomUUID())));
    }

    @Test
    void updateProgress_firstTime_scenarioCorrectness_incorrect() {
        when(evaluationResult.getScenarioCorrectness()).thenReturn(ScenarioCorrectness.incorrect);
        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<BKTPathwayProgress> captor = ArgumentCaptor.forClass(BKTPathwayProgress.class);
        when(evaluationResult.getInteractiveComplete()).thenReturn(false);

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.empty());
        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.empty());
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(0.5f).setConfidence(0.5f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        BKTPathwayProgress persistedProgress = captor.getValue();

        verifyBKTProgressValues(persistedProgress);
        verifyBKTResult(persistedProgress, 0.2d, 0.44d, 0.5d);
        verifyCompletion(persistedProgress.getCompletion(), 0.16666667f, 0.16666667f);
        verifyCompetencyMetValue(0.44f);

        assertEquals(walkableOne.getElementId(), persistedProgress.getInProgressElementId());
        assertEquals(walkableOne.getElementType(), persistedProgress.getInProgressElementType());

        assertTrue(persistedProgress.getCompletedWalkables().isEmpty());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());

    }

    @Test
    void updateProgress_firstTime_interactiveComplete() {
        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<BKTPathwayProgress> captor = ArgumentCaptor.forClass(BKTPathwayProgress.class);

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.empty());
        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.empty());
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        BKTPathwayProgress persistedProgress = captor.getValue();

        verifyBKTProgressValues(persistedProgress);
        verifyBKTResult(persistedProgress, 0.8d, 0.86d, 0.5d);
        verifyCompletion(persistedProgress.getCompletion(), 0.5f, 0.5f);
        verifyCompetencyMetValue(0.86f);

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(1, persistedProgress.getCompletedWalkables().size());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    @Test
    void updateProgress_meetBKTExitCondition() {
        BKTPathwayProgress previousProgress = mock(BKTPathwayProgress.class);

        when(previousProgress.getCompletedWalkables()).thenReturn(Lists.newArrayList(walkableOne.getElementId(), walkableThree.getElementId()));
        when(previousProgress.getChildWalkableCompletionValues()).thenReturn(new HashMap<>());
        when(previousProgress.getChildWalkableCompletionConfidences()).thenReturn(new HashMap<>());
        when(previousProgress.getpLn()).thenReturn(0.86d);

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.just(previousProgress));

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.just(previousProgress));

        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<BKTPathwayProgress> captor = ArgumentCaptor.forClass(BKTPathwayProgress.class);

        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        BKTPathwayProgress persistedProgress = captor.getValue();

        verifyBKTProgressValues(persistedProgress);
        verifyBKTResult(persistedProgress, 0.9608938547486033d, 0.9726256983240223d, 0.7160000000000001d);
        verifyCompletion(persistedProgress.getCompletion(), 1f, 1f);
        verifyCompetencyMetValue(0.9726256983240223f);

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(1, persistedProgress.getCompletedWalkables().size());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    @Test
    void updateProgress_previousProgress_repeating() {
        BKTPathwayProgress previousProgress = mock(BKTPathwayProgress.class);

        when(previousProgress.getCompletedWalkables()).thenReturn(Lists.newArrayList(walkableOne.getElementId(), walkableThree.getElementId()));
        when(previousProgress.getChildWalkableCompletionValues()).thenReturn(new HashMap<>());
        when(previousProgress.getChildWalkableCompletionConfidences()).thenReturn(new HashMap<>());
        when(previousProgress.getInProgressElementId()).thenReturn(walkableTwo.getElementId());
        when(previousProgress.getpLn()).thenReturn(0.76d);
        when(previousProgress.getpCorrect()).thenReturn(0.67);
        when(previousProgress.getpLnMinusGivenActual()).thenReturn(0.47);

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.just(previousProgress));

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.just(previousProgress));

        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<BKTPathwayProgress> captor = ArgumentCaptor.forClass(BKTPathwayProgress.class);

        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        BKTPathwayProgress persistedProgress = captor.getValue();

        verifyBKTProgressValues(persistedProgress);
        verifyBKTResult(persistedProgress, 0.47, 0.76, 0.67);
        verifyCompletion(persistedProgress.getCompletion(), 0.5f, 0.5f);

        // verify competency is not awarded in non-first attempts
        verify(competencyMetService, never()).create(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class), eq(CoursewareElementType.PATHWAY),
                any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class), any(Float.class), any(Float.class));

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(1, persistedProgress.getCompletedWalkables().size());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    @Test
    void updateProgress_meetConfiguredExitCondition() {
        BKTPathwayProgress previousProgress = mock(BKTPathwayProgress.class);

        Map<UUID, Float> completionValues = new HashMap<>();
        completionValues.put(walkableOne.getElementId(), 1f);
        completionValues.put(walkableThree.getElementId(), 1f);
        completionValues.put(walkableTwo.getElementId(), 1f);
        Map<UUID, Float> completionConfidences = new HashMap<>();
        completionConfidences.put(walkableOne.getElementId(), 1f);
        completionConfidences.put(walkableThree.getElementId(), 1f);
        completionConfidences.put(walkableTwo.getElementId(), 1f);

        when(previousProgress.getCompletedWalkables()).thenReturn(Lists.newArrayList(walkableOne.getElementId(), walkableThree.getElementId(), walkableTwo.getElementId()));
        when(previousProgress.getChildWalkableCompletionValues()).thenReturn(completionValues);
        when(previousProgress.getChildWalkableCompletionConfidences()).thenReturn(completionConfidences);
        when(previousProgress.getpLn()).thenReturn(0.56d);

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, 1))
                .thenReturn(Flux.just(previousProgress));

        when(progressService.findLatestNBKTPathway(deploymentId, pathwayId, studentId, learnerBKTPathway.getMaintainFor() - 1))
                .thenReturn(Flux.just(previousProgress));

        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<BKTPathwayProgress> captor = ArgumentCaptor.forClass(BKTPathwayProgress.class);

        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        BKTPathwayProgress persistedProgress = captor.getValue();

        verifyBKTProgressValues(persistedProgress);
        verifyBKTResult(persistedProgress, 0.8358208955223881d, 0.8850746268656717d, 0.536d);
        verifyCompletion(persistedProgress.getCompletion(), 1f, 1f);
        verifyCompetencyMetValue(0.8850746268656717f);

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(3, persistedProgress.getCompletedWalkables().size());

        assertEquals(3, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(3, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    private void verifyBKTProgressValues(final BKTPathwayProgress persistedProgress) {
        assertAll(() -> {
            assertNotNull(persistedProgress);
            assertNotNull(persistedProgress.getId());
            assertEquals(deploymentId, persistedProgress.getDeploymentId());
            assertEquals(changeId, persistedProgress.getChangeId());
            assertEquals(pathwayId, persistedProgress.getCoursewareElementId());
            assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
            assertEquals(studentId, persistedProgress.getStudentId());
            assertEquals(pathwayAttemptId, persistedProgress.getAttemptId());
            assertEquals(evaluationId, persistedProgress.getEvaluationId());
        });
    }

    private void verifyCompletion(final Completion completion, final float value, final float confidence) {
        assertAll(() -> {
            assertNotNull(completion);
            assertEquals(Float.valueOf(value), completion.getValue());
            assertEquals(Float.valueOf(confidence), completion.getConfidence());
        });
    }

    private void verifyBKTResult(final BKTPathwayProgress persistedProgress, double pLnMinusActual, double pLn, double pCorrect) {
        assertAll(() -> {
            assertEquals(Double.valueOf(pLnMinusActual), persistedProgress.getpLnMinusGivenActual());
            assertEquals(Double.valueOf(pLn), persistedProgress.getpLn());
            assertEquals(Double.valueOf(pCorrect), persistedProgress.getpCorrect());
        });
    }

    private void verifyCompetencyMetValue(final float expected) {
        ArgumentCaptor<Float> competencyMetValueCaptor = ArgumentCaptor.forClass(Float.class);
        verify(competencyMetService).create(eq(studentId), eq(deploymentId), eq(changeId), eq(pathwayId), eq(CoursewareElementType.PATHWAY),
                eq(evaluationId), eq(documentId), any(UUID.class), eq(documentItemId), eq(pathwayAttemptId), competencyMetValueCaptor.capture(), eq(1f));

        Float competencyMetValue = competencyMetValueCaptor.getValue();

        assertEquals(Float.valueOf(expected), competencyMetValue);
    }

}
