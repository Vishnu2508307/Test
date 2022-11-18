package com.smartsparrow.eval.resolver;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.eval.parser.Operand;
import com.smartsparrow.learner.data.EvaluationContext;
import com.smartsparrow.util.DataType;

import reactor.core.publisher.Mono;

public class OperandLiteralResolver implements Resolver<Operand, DataType, EvaluationContext> {

    @Inject
    public OperandLiteralResolver() {
    }

    @Override
    public Mono<Operand> resolve(final Operand resolvable, final EvaluationContext context) {
        return null;
    }

    @Trace(async = true)
    @Override
    public Mono<Operand> resolve(Operand operand, DataType operandType, EvaluationContext evaluationContext) {
        return Mono.just(operand.setResolvedValue(operand.getValue()));
    }
}
