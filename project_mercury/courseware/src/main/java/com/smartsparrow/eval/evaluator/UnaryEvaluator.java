package com.smartsparrow.eval.evaluator;

import java.util.Map;

import com.smartsparrow.eval.operator.UnaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class UnaryEvaluator implements Evaluator {

    private final Operand lhs;
    private final UnaryOperator operator;
    private final Map<String, Object> options;

    public UnaryEvaluator(Operand lhs, UnaryOperator operator, Map<String, Object> options) {
        this.lhs = lhs;
        this.operator = operator;
        this.options = options;
    }

    @Override
    public boolean evaluate() {
        return operator.test(lhs, options);
    }
}
