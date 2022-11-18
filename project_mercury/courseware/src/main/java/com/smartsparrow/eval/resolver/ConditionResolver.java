package com.smartsparrow.eval.resolver;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.parser.BaseCondition;
import com.smartsparrow.eval.parser.ChainedCondition;
import com.smartsparrow.eval.parser.Condition;
import com.smartsparrow.eval.parser.Evaluator;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.eval.parser.ResolverContext;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConditionResolver {

    private final OperandLiteralResolver operandLiteralResolver;
    private final OperandScopeResolver operandScopeResolver;

    @Inject
    public ConditionResolver(OperandLiteralResolver operandLiteralResolver,
                             OperandScopeResolver operandScopeResolver) {
        this.operandLiteralResolver = operandLiteralResolver;
        this.operandScopeResolver = operandScopeResolver;
    }

    /**
     * Recursively resolve a chained condition
     *
     * @param chainedCondition the condition to recursively resolve
     * @param evaluationContext the evaluation context for which the chained condition should be resolved
     * @return the resolved chained condition
     * @throws IllegalArgumentException      when required fields are <code>null</code> in the chained condition structure
     * @throws UnsupportedOperationException when unsupported types are found for a {@link Condition} or a
     *                                       {@link Resolver}
     */
    @Trace(async = true)
    public Mono<ChainedCondition> resolve(ChainedCondition chainedCondition, EvaluationContext evaluationContext) {

        return Mono.just(chainedCondition)
                .flatMap(condition -> Flux.just(condition.getConditions().toArray(new BaseCondition[0]))
                        .flatMap(baseCondition -> resolve(baseCondition, evaluationContext))
                        .collectList())
                .map(resolvedConditions -> new ChainedCondition()
                        .setConditions(resolvedConditions)
                        .setType(chainedCondition.getType())
                        .setOperator(chainedCondition.getOperator()));
    }

    /**
     * Determine if the condition should be resolved as a {@link ChainedCondition} or an {@link Evaluator}
     *
     * @param baseCondition  the base condition to resolve
     * @param evaluationContext the evaluation context for which the chained condition should be resolved
     * @throws UnsupportedOperationException if the condition type is not supported
     */
    @Trace(async = true)
    private Mono<? extends BaseCondition> resolve(final BaseCondition baseCondition, EvaluationContext evaluationContext) {
        Condition.Type conditionType = baseCondition.getType();
        checkArgument(conditionType != null, "type is required for a condition");
        switch (conditionType) {
            case CHAINED_CONDITION:
                return resolve((ChainedCondition) baseCondition, evaluationContext);
            case EVALUATOR:
                return resolve((Evaluator) baseCondition, evaluationContext);
            default:
                throw new UnsupportedOperationException(String.format("%s type not supported", conditionType));
        }
    }

    /**
     * Resolve both left and right operand for an {@link Evaluator} condition
     *
     * @param evaluator      the condition to evaluate the operands for
     * @param evaluationContext the evaluation context for which the evaluator should be resolved
     */
    @Trace(async = true)
    private Mono<Evaluator> resolve(Evaluator evaluator, EvaluationContext evaluationContext) {

        Mono<Operand> resolvedLhs = resolve(evaluator.getLhs(), evaluator.getOperandType(), evaluationContext);
        Mono<Operand> resolvedRhs = resolve(evaluator.getRhs(), evaluator.getOperandType(), evaluationContext);

        return Mono.zip(resolvedLhs, resolvedRhs)
                .map(tuple -> new Evaluator()
                        .setLhs(tuple.getT1())
                        .setRhs(tuple.getT2())
                        .setType(evaluator.getType())
                        .setOperandType(evaluator.getOperandType())
                        .setOperator(evaluator.getOperator())
                        .setOptions(evaluator.getOptions()));
    }

    /**
     * Read the {@link ResolverContext} of the operand to determine which implementation should be called for resolving
     * the condition
     *
     * @param operand        the operand to resolve the value for
     * @param evaluationContext the evaluation context for which the operand should be resolved
     * @param operandType the type of the Operand (one of {@link DataType})
     * @throws IllegalArgumentException      when either operand argument, the resolverContext or the resolver type are
     *                                       <code>null</code>
     * @throws UnsupportedOperationException when the resolver type is not supported
     */
    @Trace(async = true)
    private Mono<Operand> resolve(Operand operand, DataType operandType, EvaluationContext evaluationContext) {
        checkArgument(operand != null, "operand is required");
        ResolverContext resolverContext = operand.getResolver();
        checkArgument(resolverContext != null, "operand must have a non null resolverContext");
        Resolver.Type resolverType = resolverContext.getType();
        checkArgument(resolverType != null, "type is required for resolverContext");

        switch (resolverType) {
            case SCOPE:
                return operandScopeResolver.resolve(operand, operandType, evaluationContext);
            case WEB:
                throw new UnsupportedOperationException("WEB resolver not implemented");
            case LITERAL:
                return operandLiteralResolver.resolve(operand, operandType, evaluationContext);
            default:
                throw new UnsupportedOperationException("unsupported resolver type");
        }
    }
}
