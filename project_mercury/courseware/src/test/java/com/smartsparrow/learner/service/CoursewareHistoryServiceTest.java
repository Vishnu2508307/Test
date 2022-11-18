package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.EvaluationDataStub.buildEvaluationResult;
import static com.smartsparrow.learner.service.EvaluationDataStub.buildLearnerEvaluationRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.CompletedWalkableGateway;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CoursewareHistoryServiceTest {

    @InjectMocks
    private CoursewareHistoryService coursewareHistoryService;

    @Mock
    private CompletedWalkableGateway completedWalkableGateway;

    @Mock
    private AttemptService attemptService;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    private static final UUID evaluationId = UUIDs.timeBased();
    private static final CoursewareElementType walkableType = CoursewareElementType.INTERACTIVE;
    private static final EvaluationResult evaluationResult = buildEvaluationResult(true);
    private static final LearnerEvaluationRequest evaluationRequest = buildLearnerEvaluationRequest(elementId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should save the student completed interactive into a completed walkable record")
    void record() {

        ArgumentCaptor<CompletedWalkable> completedWalkableCaptor = ArgumentCaptor.forClass(CompletedWalkable.class);
        when(completedWalkableGateway.persist(any(CompletedWalkable.class))).thenReturn(Flux.just(new Void[]{}));

        coursewareHistoryService.record(studentId, evaluationResult, walkableType).block();

        verify(completedWalkableGateway).persist(completedWalkableCaptor.capture());
        CompletedWalkable captured = completedWalkableCaptor.getValue();

        assertNotNull(captured);
        assertEquals(evaluationResult.getDeployment().getId(), captured.getDeploymentId());
        assertEquals(evaluationResult.getDeployment().getChangeId(), captured.getChangeId());
        assertEquals(studentId, captured.getStudentId());
        assertEquals(evaluationResult.getParentId(), captured.getParentElementId());
        assertEquals(evaluationResult.getAttempt().getParentId(), captured.getParentElementAttemptId());
        assertEquals(evaluationResult.getCoursewareElementId(), captured.getElementId());
        assertEquals(evaluationResult.getId(), captured.getEvaluationId());
        assertEquals(evaluationResult.getAttemptId(), captured.getElementAttemptId());
        assertEquals(CoursewareElementType.PATHWAY, captured.getParentElementType());
        assertEquals(walkableType, captured.getElementType());
        assertNotNull(captured.getEvaluatedAt());
    }

    @Test
    @DisplayName("It should save the student completed interactive into a completed walkable record from evaluation request")
    void recordRequest() {
        final UUID pathwayId = UUIDs.timeBased();
        final Attempt attempt = new Attempt()
                .setId(UUIDs.timeBased())
                .setParentId(UUIDs.timeBased());
        ArgumentCaptor<CompletedWalkable> completedWalkableCaptor = ArgumentCaptor.forClass(CompletedWalkable.class);
        when(completedWalkableGateway.persist(any(CompletedWalkable.class))).thenReturn(Flux.just(new Void[]{}));

        coursewareHistoryService.record(evaluationId,
                evaluationRequest,
                new CoursewareElement()
                        .setElementId(elementId)
                        .setElementType(CoursewareElementType.INTERACTIVE),
                attempt,
                pathwayId
        ).block();

        verify(completedWalkableGateway).persist(completedWalkableCaptor.capture());
        CompletedWalkable captured = completedWalkableCaptor.getValue();

        assertNotNull(captured);
        assertEquals(evaluationRequest.getDeployment().getId(), captured.getDeploymentId());
        assertEquals(evaluationRequest.getDeployment().getChangeId(), captured.getChangeId());
        assertEquals(evaluationRequest.getStudentId(), captured.getStudentId());
        assertEquals(attempt.getParentId(), captured.getParentElementAttemptId());
        assertEquals(evaluationRequest.getLearnerWalkable().getId(), captured.getElementId());
        assertEquals(evaluationId, captured.getEvaluationId());
        assertEquals(attempt.getId(), captured.getElementAttemptId());
        assertEquals(CoursewareElementType.PATHWAY, captured.getParentElementType());
        assertEquals(CoursewareElementType.INTERACTIVE, captured.getElementType());
        assertNotNull(captured.getEvaluatedAt());
    }

    @Test
    void fetchHistory_attemptNotFound() {
        LearnerPathway learnerPathway = mock(LearnerPathway.class);
        when(learnerPathway.getId()).thenReturn(UUID.randomUUID());
        when(learnerPathway.getChangeId()).thenReturn(UUID.randomUUID());
        when(learnerPathway.getDeploymentId()).thenReturn(UUID.randomUUID());

        when(attemptService.findLatestAttempt(any(UUID.class), any(UUID.class), eq(studentId)))
                .thenReturn(Mono.error(new AttemptNotFoundFault("not found")));

        List<CompletedWalkable> found = coursewareHistoryService.fetchHistory(learnerPathway, studentId)
                .collectList()
                .block();

        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    void fetchHistory() {
        UUID parentAttemptId = UUID.randomUUID();

        when(attemptService.findLatestAttempt(any(UUID.class), any(UUID.class), eq(studentId)))
                .thenReturn(Mono.just(new Attempt().setId(parentAttemptId)));

        // from new table
        when(completedWalkableGateway.findAll(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.just(
                        new CompletedWalkable()
                                .setElementId(elementId)
                                .setStudentId(studentId),
                        new CompletedWalkable()
                                .setElementId(UUID.randomUUID())
                                .setStudentId(UUID.randomUUID())
                ));

        LearnerPathway learnerPathway = mock(LearnerPathway.class);
        when(learnerPathway.getId()).thenReturn(UUID.randomUUID());
        when(learnerPathway.getChangeId()).thenReturn(UUID.randomUUID());
        when(learnerPathway.getDeploymentId()).thenReturn(UUID.randomUUID());

        List<CompletedWalkable> found = coursewareHistoryService.fetchHistory(learnerPathway, studentId)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(2, found.size());
    }
}
