package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.elementId;
import static com.smartsparrow.learner.service.UpdateCoursewareElementProgressEventDataStub.progressEvent;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerGraphPathway;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.WalkableProgress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

@Disabled
class UpdateGraphPathwayProgressHandlerTest {

    @InjectMocks
    private UpdateGraphPathwayProgressHandler handler;

    @Mock
    private ProgressService progressService;
    @Mock
    private AttemptService attemptService;
    @Mock
    private LearnerPathwayService learnerPathwayService;

    private UpdateGraphPathwayProgressHandler spy;
    private Exchange exchange;
    private UpdateCoursewareElementProgressEvent event;
    private LearnerGraphPathway learnerGraphPathway;

    private static final UUID walkableIdOne = UUID.randomUUID();
    private static final UUID walkableIdTwo = UUID.randomUUID();
    private static final WalkableChild startingWalkable = new WalkableChild()
            .setElementId(UUID.randomUUID())
            .setElementType(CoursewareElementType.INTERACTIVE);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        spy = mockProgressHandler(handler);

        learnerGraphPathway = mock(LearnerGraphPathway.class);

        when(learnerGraphPathway.getConfiguredWalkable()).thenReturn(Flux.just(startingWalkable));

        when(progressService.persist(any(GraphPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));

        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE)
                .setElementId(walkableIdOne)
                .setElementType(CoursewareElementType.INTERACTIVE);

        event = progressEvent(new CoursewareElement().setElementId(elementId), CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event,
                ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                elementId,
                progressActionContext);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(walkableIdTwo)
                ));
    }

    @Test
    void updateProgress_directChild_COMPLETE_PATHWAY() {

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(1), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(1), persistedProgress.getCompletion().getConfidence());
        assertEquals(startingWalkable.getElementId(), persistedProgress.getCurrentWalkableId());
        assertEquals(startingWalkable.getElementType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertNotNull(persistedProgress.getAttemptId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

    @Test
    void updateProgress_ACTIVITY_COMPLETE_AND_GO_TO() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_PATHWAY_COMPLETE)
                .setElementId(walkableIdOne)
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.ACTIVITY_COMPLETE_AND_GO_TO, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(), startingWalkable.getElementId(), progressActionContext);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                     .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(walkableIdTwo)
                                .setElementType(CoursewareElementType.ACTIVITY),
                        new WalkableChild()
                                .setElementId(progressActionContext.getElementId())
                                .setElementType(CoursewareElementType.INTERACTIVE)
                ));

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getConfidence());
//        assertEquals(event.getProgressActionContext().getElementId(), persistedProgress.getCurrentWalkableId());
//        assertEquals(event.getProgressActionContext().getElementType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

    @Test
    void updateProgress_directChild_INTERACTIVE_COMPLETE_AND_GO_TO() {
        final UUID walkableIdThree = UUID.randomUUID();
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO, true);
        exchange = UpdateCoursewareElementProgressEventDataStub
                .mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                        walkableIdThree,
                        progressActionContext
                        );

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(walkableIdTwo)
                                .setElementType(CoursewareElementType.ACTIVITY),
                        new WalkableChild()
                                .setElementId(walkableIdThree)
                                .setElementType(CoursewareElementType.INTERACTIVE)
                ));

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getConfidence());
//        assertEquals(event.getProgressActionContext().getElementId(), persistedProgress.getCurrentWalkableId());
//        assertEquals(event.getProgressActionContext().getElementType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

    @Test
    void updateProgress_directChild_INTERACTIVE_COMPLETE_AND_GO_TO_differentPathway() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                elementId,
                progressActionContext
                );

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE)
                ));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> spy.updateProgress(exchange));

        assertNotNull(f);
        assertEquals("cannot GO_TO a walkable outside the current pathway", f.getMessage());
    }

    @Test
    void updateProgress_directChild_anyOtherProgressionType() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_REPEAT)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);
        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_REPEAT, true);
        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                 elementId,
                progressActionContext
                );

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(walkableIdTwo)
                                .setElementType(CoursewareElementType.ACTIVITY),
                        new WalkableChild()
                                .setElementId(progressActionContext.getElementId())
                                .setElementType(CoursewareElementType.INTERACTIVE)
                ));

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getConfidence());
        assertEquals(startingWalkable.getElementId(), persistedProgress.getCurrentWalkableId());
        assertEquals(startingWalkable.getElementType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

    @Test
    void updateProgress_fromDescendant() {
        final ProgressActionContext progressActionContext = new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO)
                .setElementId(UUID.randomUUID())
                .setElementType(CoursewareElementType.INTERACTIVE);

        event = progressEvent(null, CoursewareElementType.PATHWAY, ProgressionType.INTERACTIVE_COMPLETE_AND_GO_TO, true);
        List<Progress> previousProgresses = event.getEventProgress();
        Progress progress = new WalkableProgress()
                .setAttemptId(event.getUpdateProgressEvent().getAttemptId())
                .setChangeId(event.getUpdateProgressEvent().getChangeId())
                .setDeploymentId(event.getUpdateProgressEvent().getDeploymentId())
                .setCoursewareElementId(UUID.randomUUID())
                .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                .setStudentId(event.getUpdateProgressEvent().getStudentId())
                .setEvaluationId(event.getUpdateProgressEvent().getEvaluationId())
                .setCompletion(new Completion()
                        .setConfidence(1f)
                        .setValue(1f));
        previousProgresses.add(progress);
        event.setEventProgress(previousProgresses);

        exchange = UpdateCoursewareElementProgressEventDataStub.mockExchangeFrom(event, ((EvaluationEventMessage) event.getUpdateProgressEvent()).getEvaluationResult(),
                event.getElement().getElementId(),
                progressActionContext);

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.just(event.getUpdateProgressEvent().getAttempt()));
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));
        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        when(learnerPathwayService.findWalkables(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId()))
                .thenReturn(Flux.just(
                        new WalkableChild()
                                .setElementId(walkableIdOne)
                                .setElementType(CoursewareElementType.INTERACTIVE),
                        new WalkableChild()
                                .setElementId(walkableIdTwo)
                                .setElementType(CoursewareElementType.ACTIVITY),
                        new WalkableChild()
                                .setElementId(progressActionContext.getElementId())
                                .setElementType(CoursewareElementType.INTERACTIVE)
                ));

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(0.33333334f), persistedProgress.getCompletion().getConfidence());
        assertEquals(startingWalkable.getElementId(), persistedProgress.getCurrentWalkableId());
        assertEquals(startingWalkable.getElementType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }

    @Test
    void updateProgress_fromDescendant_withPreviousProgress() {

        GraphPathwayProgress previousProgress = new GraphPathwayProgress()
                .setCurrentWalkableId(UUID.randomUUID())
                .setCurrentWalkableType(CoursewareElementType.INTERACTIVE);

        when(progressService.findLatestGraphPathway(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(previousProgress));

        ArgumentCaptor<GraphPathwayProgress> graphProgressCaptor = ArgumentCaptor.forClass(GraphPathwayProgress.class);

        spy.updateProgress(exchange);

        verify(progressService).persist(graphProgressCaptor.capture());

        GraphPathwayProgress persistedProgress = graphProgressCaptor.getValue();

        assertNotNull(persistedProgress);
        assertEquals(Float.valueOf(1), persistedProgress.getCompletion().getValue());
        assertEquals(Float.valueOf(1), persistedProgress.getCompletion().getConfidence());
        assertEquals(previousProgress.getCurrentWalkableId(), persistedProgress.getCurrentWalkableId());
        assertEquals(previousProgress.getCurrentWalkableType(), persistedProgress.getCurrentWalkableType());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(event.getElement().getElementId(), persistedProgress.getCoursewareElementId());
        assertEquals(event.getUpdateProgressEvent().getStudentId(), persistedProgress.getStudentId());
        assertEquals(event.getUpdateProgressEvent().getAttempt().getParentId(), persistedProgress.getAttemptId());
        assertNotNull(persistedProgress.getId());
        assertEquals(event.getUpdateProgressEvent().getDeploymentId(), persistedProgress.getDeploymentId());
        assertEquals(event.getUpdateProgressEvent().getChangeId(), persistedProgress.getChangeId());
    }


    @Test
    void updateProgress_nullPathway() {
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.empty());

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> spy.updateProgress(exchange));

        assertNotNull(f);
        assertEquals("something is wrong. The pathway must exists", f.getMessage());
    }

    @Test
    void updateProgress_nullStartingWalkable() {

        TestPublisher<WalkableChild> publisher = TestPublisher.create();
        publisher.error(new IllegalStateFault("blah"));

        when(learnerGraphPathway.getConfiguredWalkable()).thenReturn(publisher.flux());

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> spy.updateProgress(exchange));

        assertNotNull(f);
        assertEquals("blah", f.getMessage());
    }

    @Test
    void updateProgress_nullChildAttempt() {
        when(learnerPathwayService
                .find(event.getElement().getElementId(), event.getUpdateProgressEvent().getDeploymentId(), LearnerGraphPathway.class))
                .thenReturn(Mono.just(learnerGraphPathway));

        when(attemptService.findById(any(UUID.class))).thenReturn(Mono.empty());

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> spy.updateProgress(exchange));

        assertNotNull(f);
        assertEquals("something is wrong. The attempt should exists", f.getMessage());
    }
}
