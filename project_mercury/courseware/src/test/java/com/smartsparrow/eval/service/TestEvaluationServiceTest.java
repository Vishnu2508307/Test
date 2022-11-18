package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.data.TestEvaluationRequest;
import com.smartsparrow.eval.data.TestEvaluationResponse;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.learner.data.EvaluationTestContext;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class TestEvaluationServiceTest {

    @InjectMocks
    private TestEvaluationService testEvaluationService;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;

    TestEvaluationRequest testEvaluationRequest;

    @Mock
    Walkable walkable;

    private static final UUID walkableId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(walkable.getId()).thenReturn(walkableId);
        when(walkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);

        testEvaluationRequest = new TestEvaluationRequest()
                .setData("{\"some\":\"data\"}")
                .setWalkable(walkable)
                .setScenarioLifecycle(ScenarioLifecycle.INTERACTIVE_EVALUATE);
    }

    @Test
    void evaluate() {

        final Scenario scenario = new Scenario()
                .setId(UUIDs.timeBased());
        final ScenarioEvaluationResult scenarioResult = new ScenarioEvaluationResult()
                .setScenarioId(scenario.getId());

        ArgumentCaptor<EvaluationTestContext> contextCaptor = ArgumentCaptor.forClass(EvaluationTestContext.class);

        when(scenarioService.findAll(walkableId, ScenarioLifecycle.INTERACTIVE_EVALUATE))
                .thenReturn(Flux.just(scenario));
        when(scenarioEvaluationService.evaluateCondition(eq(scenario), any(EvaluationTestContext.class)))
                .thenReturn(Mono.just(scenarioResult));

        final TestEvaluationResponse result = testEvaluationService.evaluate(testEvaluationRequest)
                .block();

        assertNotNull(result);
        assertEquals(1, result.getScenarioEvaluationResults().size());
        assertEquals(scenarioResult, result.getScenarioEvaluationResults().get(0));
        assertEquals(testEvaluationRequest, result.getEvaluationRequest());

        verify(scenarioService).findAll(walkableId, ScenarioLifecycle.INTERACTIVE_EVALUATE);
        verify(scenarioEvaluationService).evaluateCondition(eq(scenario), contextCaptor.capture());

        final EvaluationTestContext capturedContext = contextCaptor.getValue();

        assertNotNull(capturedContext);
        assertEquals(EvaluationContext.Type.TEST, capturedContext.getType());
        assertEquals(testEvaluationRequest.getData(), capturedContext.getData());

    }

}