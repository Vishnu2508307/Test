package com.smartsparrow.eval.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operator.Operator;
import com.smartsparrow.eval.parser.BaseCondition;
import com.smartsparrow.eval.parser.ChainedCondition;
import com.smartsparrow.eval.parser.Condition;
import com.smartsparrow.eval.parser.Evaluator;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.eval.parser.ScopeContext;
import com.smartsparrow.learner.data.EvaluationLearnerContext;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

class ConditionResolverTest {

    @InjectMocks
    private ConditionResolver conditionResolver;

    @Mock
    private OperandLiteralResolver operandLiteralResolver;

    @Mock
    private OperandScopeResolver operandScopeResolver;

    private ChainedCondition chainedCondition;
    private EvaluationLearnerContext evaluationLearnerContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        List<BaseCondition> evaluators = Lists.newArrayList(
                new Evaluator()
                        .setType(Condition.Type.EVALUATOR)
                        .setOperator(Operator.Type.CONTAINS)
                        .setOperandType(DataType.STRING)
                        .setOptions(new ArrayList<>())
                        .setLhs(new Operand()
                                .setResolver(new ScopeContext()
                                        .setType(Resolver.Type.SCOPE)))
                        .setRhs(new Operand()
                                .setResolver(new LiteralContext()
                                        .setType(Resolver.Type.LITERAL))
                                .setValue("wow"))
        );

        List<BaseCondition> childChainedConditions = Lists.newArrayList(
                new ChainedCondition()
                        .setConditions(evaluators)
                        .setType(Condition.Type.CHAINED_CONDITION)
                        .setOperator(Operator.Type.OR),
                new ChainedCondition()
                        .setConditions(evaluators)
                        .setType(Condition.Type.CHAINED_CONDITION)
                        .setOperator(Operator.Type.AND)
        );

        chainedCondition = new ChainedCondition()
                .setType(Condition.Type.CHAINED_CONDITION)
                .setOperator(Operator.Type.AND)
                .setConditions(childChainedConditions);

        evaluationLearnerContext = mock(EvaluationLearnerContext.class);

        when(operandLiteralResolver.resolve(any(Operand.class),any(DataType.class), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(new Operand()));
        when(operandScopeResolver.resolve(any(Operand.class),any(DataType.class), any(EvaluationLearnerContext.class))).thenReturn(Mono.just(new Operand()));
    }

    @Test
    @DisplayName("it should successfully resolve the literal values for each operand")
    void resolve_success() {

        ChainedCondition condition = conditionResolver.resolve(chainedCondition, evaluationLearnerContext).block();

        assertNotNull(condition);
        verify(operandLiteralResolver, times(2))
                .resolve(any(Operand.class),any(DataType.class), any(EvaluationLearnerContext.class));
        verify(operandScopeResolver, times(2))
                .resolve(any(Operand.class),any(DataType.class), any(EvaluationLearnerContext.class));
    }

    @Test
    @DisplayName("it should throw an exception when the structure is missing some required fields")
    void resolve_conditionWithNullType() {
        ChainedCondition chainedCondition = new ChainedCondition()
                .setConditions(Lists.newArrayList(new ChainedCondition()
                        .setType(null)
                        .setOperator(null)
                        .setConditions(new ArrayList<>())))
                .setType(Condition.Type.CHAINED_CONDITION)
                .setOperator(Operator.Type.CONTAINS);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> conditionResolver.resolve(chainedCondition, evaluationLearnerContext).block());

        assertEquals("type is required for a condition", e.getMessage());
        verify(operandLiteralResolver, never()).resolve(any(Operand.class),any(DataType.class), eq(evaluationLearnerContext));
    }

    @Test
    @DisplayName("it should throw an exception when the resolverContext is null")
    void resolve_operandWithNullResolverContext() {
        ChainedCondition chainedCondition = new ChainedCondition()
                .setType(Condition.Type.CHAINED_CONDITION)
                .setOperator(Operator.Type.AND)
                .setConditions(Lists.newArrayList(
                        new Evaluator()
                        .setType(Condition.Type.EVALUATOR)
                        .setLhs(new Operand())
                        .setRhs(new Operand())
                ));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> conditionResolver.resolve(chainedCondition, evaluationLearnerContext).block());

        assertEquals("operand must have a non null resolverContext", e.getMessage());
        verify(operandLiteralResolver, never()).resolve(any(Operand.class),any(DataType.class), any(EvaluationLearnerContext.class));
    }

}
