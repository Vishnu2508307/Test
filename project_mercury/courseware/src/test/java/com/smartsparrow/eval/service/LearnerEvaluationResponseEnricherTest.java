package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.StudentScopeService;

import reactor.core.publisher.Mono;

class LearnerEvaluationResponseEnricherTest {

    @InjectMocks
    private LearnerEvaluationResponseEnricher enricher;

    @Mock
    private LearnerScenarioActionEnricher learnerScenarioActionEnricher;

    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    @Mock
    private StudentScopeService studentScopeService;

    @Mock
    private LearnerWalkable learnerWalkable;

    @Mock
    private LearnerEvaluationResponse response;

    private LearnerEvaluationResponseContext responseContext;
    private LearnerEvaluationRequest request;
    private WalkableEvaluationResult walkableEvaluationResult;
    private ChangeScoreAction changeScoreAction;
    private ProgressAction progressCompleted;
    private ProgressAction progressIncomplete;

    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID walkableId = UUIDs.timeBased();
    private static final UUID studentScopeUrn = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final List<CoursewareElement> ancestry = Lists.newArrayList(
            CoursewareElement.from(UUIDs.timeBased(), CoursewareElementType.ACTIVITY)
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        changeScoreAction = new ChangeScoreAction();
        progressCompleted = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.INTERACTIVE_COMPLETE));
        progressIncomplete = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.INTERACTIVE_REPEAT));

        walkableEvaluationResult = new WalkableEvaluationResult()
                .setWalkableType(CoursewareElementType.INTERACTIVE)
                .setTriggeredActions(Lists.newArrayList(changeScoreAction, progressIncomplete, progressCompleted));

        when(learnerWalkable.getDeploymentId()).thenReturn(deploymentId);
        when(learnerWalkable.getId()).thenReturn(walkableId);
        when(learnerWalkable.getStudentScopeURN()).thenReturn(studentScopeUrn);

        request = new LearnerEvaluationRequest()
                .setStudentId(studentId)
                .setLearnerWalkable(learnerWalkable);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(request)
                .setWalkableEvaluationResult(walkableEvaluationResult));

        when(learnerCoursewareService.getAncestry(eq(deploymentId), eq(walkableId), any(CoursewareElementType.class)))
                .thenReturn(Mono.just(ancestry));
        when(studentScopeService.findLatestEntries(deploymentId, studentId, studentScopeUrn))
                .thenReturn(Mono.just(new HashMap<UUID, String>(){{put(walkableId, "foo");}}));
    }

    @Test
    void enrich_nullContext() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> enricher.enrich(null));

        assertEquals("responseContext is required", f.getMessage());
    }

    @Test
    void enrich_completedChangeProgressType_activity() {
        progressCompleted = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.ACTIVITY_COMPLETE_AND_GO_TO));
        progressIncomplete = new ProgressAction()
                .setType(Action.Type.CHANGE_PROGRESS)
                .setContext(new ProgressActionContext()
                        .setProgressionType(ProgressionType.ACTIVITY_REPEAT));

        walkableEvaluationResult = new WalkableEvaluationResult()
                .setWalkableType(CoursewareElementType.ACTIVITY)
                .setTriggeredActions(Lists.newArrayList(changeScoreAction, progressIncomplete, progressCompleted));

        when(learnerWalkable.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);
        request = new LearnerEvaluationRequest()
                .setStudentId(studentId)
                .setLearnerWalkable(learnerWalkable);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(request)
                        .setWalkableEvaluationResult(walkableEvaluationResult));

        when(learnerScenarioActionEnricher.enrich(responseContext))
                .thenReturn(Mono.just(responseContext));

        final LearnerEvaluationResponseContext enrichedResponseContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedResponseContext);
        verify(learnerScenarioActionEnricher).enrich(responseContext);

        assertTrue(enrichedResponseContext.getResponse().getWalkableEvaluationResult().isWalkableComplete());
        assertEquals(CoursewareElementType.ACTIVITY, enrichedResponseContext.getResponse().getWalkableEvaluationResult().getWalkableType());
        assertEquals(ancestry, enrichedResponseContext.getAncestry());
        assertEquals(new HashMap<UUID, String>(){{put(walkableId, "foo");}}, enrichedResponseContext.getScopeEntriesMap());
        verify(learnerCoursewareService).getAncestry(deploymentId, walkableId, CoursewareElementType.ACTIVITY);
        verify(studentScopeService).findLatestEntries(deploymentId, studentId, studentScopeUrn);
    }

    @Test
    void enrich_completedChangeProgressType_interactive() {
        when(learnerWalkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);
        when(learnerScenarioActionEnricher.enrich(responseContext))
                .thenReturn(Mono.just(responseContext));

        final LearnerEvaluationResponseContext enrichedResponseContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedResponseContext);
        verify(learnerScenarioActionEnricher).enrich(responseContext);

        assertTrue(enrichedResponseContext.getResponse().getWalkableEvaluationResult().isWalkableComplete());
        assertEquals(CoursewareElementType.INTERACTIVE, enrichedResponseContext.getResponse().getWalkableEvaluationResult().getWalkableType());
        assertEquals(ancestry, enrichedResponseContext.getAncestry());
        assertEquals(new HashMap<UUID, String>(){{put(walkableId, "foo");}}, enrichedResponseContext.getScopeEntriesMap());
        verify(learnerCoursewareService).getAncestry(deploymentId, walkableId, CoursewareElementType.INTERACTIVE);
        verify(studentScopeService).findLatestEntries(deploymentId, studentId, studentScopeUrn);
    }

    @Test
    void enrich_notCompleted() {
        when(learnerWalkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);
        walkableEvaluationResult = new WalkableEvaluationResult()
                .setWalkableType(CoursewareElementType.INTERACTIVE)
                .setTriggeredActions(Lists.newArrayList(changeScoreAction, progressIncomplete));

        request = new LearnerEvaluationRequest()
                .setStudentId(studentId)
                .setLearnerWalkable(learnerWalkable);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(request)
                        .setWalkableEvaluationResult(walkableEvaluationResult));

        when(learnerScenarioActionEnricher.enrich(responseContext))
                .thenReturn(Mono.just(responseContext));

        final LearnerEvaluationResponseContext enrichedResponseContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedResponseContext);
        verify(learnerScenarioActionEnricher).enrich(responseContext);

        assertFalse(enrichedResponseContext.getResponse().getWalkableEvaluationResult().isWalkableComplete());
        assertEquals(CoursewareElementType.INTERACTIVE, enrichedResponseContext.getResponse().getWalkableEvaluationResult().getWalkableType());
        assertEquals(ancestry, enrichedResponseContext.getAncestry());
        assertEquals(new HashMap<UUID, String>(){{put(walkableId, "foo");}}, enrichedResponseContext.getScopeEntriesMap());
        verify(learnerCoursewareService).getAncestry(deploymentId, walkableId, CoursewareElementType.INTERACTIVE);
        verify(studentScopeService).findLatestEntries(deploymentId, studentId, studentScopeUrn);
    }

}