package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.ActionResolver;
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
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerPathwayService;

import reactor.core.publisher.Mono;

class LearnerScenarioActionEnricherTest {

    @InjectMocks
    private LearnerScenarioActionEnricher enricher;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ActionDeserializer actionDeserializer;

    @Mock
    private ActionResolver actionResolver;

    @Mock
    private LearnerWalkable walkable;

    @Mock
    private ScenarioEvaluationResult resultOne;

    @Mock
    private LearnerPathway learnerPathway;

    @Mock
    private LearnerEvaluationResponse learnerEvaluationResponse;

    @Mock
    private LearnerEvaluationRequest learnerEvaluationRequest;

    private LearnerEvaluationResponseContext responseContext;
    private WalkableEvaluationResult result;

    private static final ProgressAction progressOne = new ProgressAction()
            .setType(Action.Type.CHANGE_PROGRESS);
    private static final ProgressAction progressTwo = new ProgressAction()
            .setType(Action.Type.CHANGE_PROGRESS);
    private static final ChangeScoreAction scoreAction = new ChangeScoreAction();
    private static final UUID walkableId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(walkable.getId()).thenReturn(walkableId);
        when(walkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);
        when(walkable.getDeploymentId()).thenReturn(deploymentId);
        when(learnerEvaluationRequest.getParentPathwayId()).thenReturn(parentPathwayId);
        when(learnerEvaluationRequest.getLearnerWalkable()).thenReturn(walkable);
        when(actionResolver.resolve(any(Action.class), any(LearnerEvaluationResponseContext.class)))
                .thenAnswer((Answer<Mono<Action>>) invocation -> Mono.just(invocation.getArgument(0)));

        when(learnerPathway.getDefaultAction(CoursewareElementType.INTERACTIVE)).thenReturn(progressOne);

        result = new WalkableEvaluationResult()
                .setWalkableId(walkableId)
                .setWalkableType(CoursewareElementType.INTERACTIVE);

        when(resultOne.getActions()).thenReturn("actions");
        when(resultOne.getEvaluationResult()).thenReturn(true);
        when(actionDeserializer.deserialize("actions")).thenReturn(Lists.newArrayList(progressOne, progressTwo, scoreAction));

        when(learnerPathwayService.find(parentPathwayId, deploymentId)).thenReturn(Mono.just(learnerPathway));

        when(learnerEvaluationResponse.getWalkableEvaluationResult()).thenReturn(result);
        when(learnerEvaluationResponse.getScenarioEvaluationResults()).thenReturn(Lists.newArrayList(resultOne));
        when(learnerEvaluationResponse.getEvaluationRequest()).thenReturn(learnerEvaluationRequest);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(learnerEvaluationResponse);

    }

    @Test
    void enrich_nullContext() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> enricher.enrich(null));

        assertEquals("responseContext is required", f.getMessage());
    }

    @Test
    void enrich_noScenariosEvaluated() {
        when(learnerEvaluationResponse.getWalkableEvaluationResult()).thenReturn(result);
        when(learnerEvaluationResponse.getScenarioEvaluationResults()).thenReturn(new ArrayList<>());
        when(learnerEvaluationResponse.getEvaluationRequest()).thenReturn(learnerEvaluationRequest);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(learnerEvaluationResponse);

        final LearnerEvaluationResponseContext enrichedContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedContext);
        assertEquals(learnerPathway, responseContext.getParentPathway());
        assertNotNull(responseContext.getEvaluationActionState());
        assertEquals(CoursewareElement.from(walkableId, CoursewareElementType.INTERACTIVE), responseContext.getEvaluationActionState().getCoursewareElement());
        assertEquals(progressOne.getContext(), responseContext.getEvaluationActionState().getProgressActionContext());
        assertEquals(Lists.newArrayList(progressOne), responseContext.getResponse().getWalkableEvaluationResult().getTriggeredActions());
    }

    @Test
    void enrich_multipleChangeProgressActions() {

        final LearnerEvaluationResponseContext enrichedContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedContext);
        assertNull(responseContext.getParentPathway());
        assertNotNull(responseContext.getEvaluationActionState());
        assertEquals(CoursewareElement.from(walkableId, CoursewareElementType.INTERACTIVE), responseContext.getEvaluationActionState().getCoursewareElement());
        assertEquals(progressOne.getContext(), responseContext.getEvaluationActionState().getProgressActionContext());
        assertEquals(Lists.newArrayList(progressOne, scoreAction), responseContext.getResponse().getWalkableEvaluationResult().getTriggeredActions());
    }

    @Test
    void enrich_oneChangeProgressAction() {
        when(actionDeserializer.deserialize("actions")).thenReturn(Lists.newArrayList(progressTwo));

        final LearnerEvaluationResponseContext enrichedContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedContext);
        assertNull(responseContext.getParentPathway());
        assertNotNull(responseContext.getEvaluationActionState());
        assertEquals(CoursewareElement.from(walkableId, CoursewareElementType.INTERACTIVE), responseContext.getEvaluationActionState().getCoursewareElement());
        assertEquals(progressTwo.getContext(), responseContext.getEvaluationActionState().getProgressActionContext());
        assertEquals(Lists.newArrayList(progressTwo), responseContext.getResponse().getWalkableEvaluationResult().getTriggeredActions());
    }

    @Test
    void enrich_noChangeProgressAction() {
        when(actionDeserializer.deserialize("actions")).thenReturn(Lists.newArrayList(scoreAction));

        final LearnerEvaluationResponseContext enrichedContext = enricher.enrich(responseContext)
                .block();

        assertNotNull(enrichedContext);
        assertNull(responseContext.getParentPathway());
        assertNotNull(responseContext.getEvaluationActionState());
        assertEquals(CoursewareElement.from(walkableId, CoursewareElementType.INTERACTIVE), responseContext.getEvaluationActionState().getCoursewareElement());
        assertEquals(new ProgressActionContext()
                .setProgressionType(ProgressionType.INTERACTIVE_REPEAT), responseContext.getEvaluationActionState().getProgressActionContext());
        assertEquals(2, responseContext.getResponse().getWalkableEvaluationResult().getTriggeredActions().size());
    }

}