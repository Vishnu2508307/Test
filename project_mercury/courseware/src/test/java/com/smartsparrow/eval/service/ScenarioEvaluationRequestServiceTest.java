package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.lang.ScenarioConditionParserFault;
import com.smartsparrow.eval.condition.ConditionEvaluator;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.deserializer.ConditionDeserializer;
import com.smartsparrow.eval.lang.ScenarioEvaluationException;
import com.smartsparrow.eval.lang.UnableToResolveException;
import com.smartsparrow.eval.parser.ChainedCondition;
import com.smartsparrow.eval.parser.Evaluator;
import com.smartsparrow.eval.resolver.ConditionResolver;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.learner.data.LearnerScenario;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ScenarioEvaluationRequestServiceTest {

    private static final UUID learnerScenarioId = UUID.randomUUID();
    private static final String condition = "condition";
    private EvaluationLearnerContext evaluationLearnerContext;
    private LearnerScenario learnerScenario;
    private ChainedCondition deserializedCondition;
    private ChainedCondition resolvedCondition;

    @InjectMocks
    ScenarioEvaluationService scenarioEvaluationService;

    @Mock
    ConditionDeserializer conditionDeserializer;

    @Mock
    ConditionResolver conditionResolver;

    @Mock
    ConditionEvaluator conditionEvaluator;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        evaluationLearnerContext = mock(EvaluationLearnerContext.class);
        learnerScenario = new LearnerScenario()
                .setCondition(condition)
                .setId(learnerScenarioId);

        Evaluator evaluator = mock(Evaluator.class);

        deserializedCondition = new ChainedCondition()
                .setConditions(Lists.newArrayList(evaluator));
        resolvedCondition = mock(ChainedCondition.class);

        when(conditionDeserializer.deserialize(condition)).thenReturn(Mono.just(deserializedCondition));
        when(conditionResolver.resolve(deserializedCondition, evaluationLearnerContext)).thenReturn(Mono.just(resolvedCondition));
        when(conditionEvaluator.evaluate(resolvedCondition)).thenReturn(true);

    }

    @Test
    void evaluateCondition_deserializerFails() {
        TestPublisher<ChainedCondition> publisher = TestPublisher.create();
        publisher.error(new ScenarioConditionParserFault(new IllegalArgumentException()));
        when(conditionDeserializer.deserialize(condition)).thenReturn(publisher.mono());

        assertThrows(ScenarioEvaluationException.class,
                () -> scenarioEvaluationService.evaluateCondition(learnerScenario, evaluationLearnerContext).block());

    }

    @Test
    void evaluateCondition_resolverFails() {
        TestPublisher<ChainedCondition> publisher = TestPublisher.create();
        publisher.error(new UnableToResolveException("scope id not found"));
        when(conditionResolver.resolve(deserializedCondition, evaluationLearnerContext)).thenReturn(publisher.mono());

        ScenarioEvaluationResult scenarioEvaluationResult = scenarioEvaluationService
                .evaluateCondition(learnerScenario, evaluationLearnerContext).block();

        assertNotNull(scenarioEvaluationResult);
        assertEquals("scope id not found", scenarioEvaluationResult.getErrorMessage());
    }

    @Test
    void evaluateCondition_evaluationFails() {
        doThrow(new UnsupportedOperationException("IS Operation not supported for supplied operand types"))
                .when(conditionEvaluator).evaluate(resolvedCondition);

        ScenarioEvaluationResult scenarioEvaluationResult = scenarioEvaluationService
                .evaluateCondition(learnerScenario, evaluationLearnerContext).block();

        assertNotNull(scenarioEvaluationResult);
        assertEquals("IS Operation not supported for supplied operand types",
                scenarioEvaluationResult.getErrorMessage());
    }

    @Test
    void evaluateCondition_ConditionTypeNotFound() {
        TestPublisher<ChainedCondition> publisher = TestPublisher.create();
        publisher.error(new ScenarioConditionParserFault(new IllegalArgumentException()));
        when(conditionDeserializer.deserialize(anyString())).thenReturn(publisher.mono());
        doThrow(new RuntimeException())
                .when(conditionEvaluator).evaluate(resolvedCondition);

        assertThrows(ScenarioEvaluationException.class,
                () -> scenarioEvaluationService.evaluateCondition(learnerScenario, evaluationLearnerContext).block());
    }

    @Test
    void evaluateCondition_success() {
        ScenarioEvaluationResult result = scenarioEvaluationService.evaluateCondition(learnerScenario, evaluationLearnerContext)
                .block();

        assertNotNull(result);
        assertEquals(learnerScenarioId, result.getScenarioId());
    }

    @Test
    void evaluateCondition_emptyConditions() {
        when(conditionDeserializer.deserialize(anyString())).thenReturn(Mono.just(new ChainedCondition().setConditions(new ArrayList<>())));

        ScenarioEvaluationResult result = scenarioEvaluationService.evaluateCondition(learnerScenario, evaluationLearnerContext)
                .block();

        assertNotNull(result);
        assertTrue(result.getEvaluationResult());
    }

}
