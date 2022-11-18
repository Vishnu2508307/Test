package com.smartsparrow.eval.evaluator;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class BinaryEvaluator implements Evaluator {

    private final Operand lhs;
    private final Operand rhs;
    private final BinaryOperator operator;
    private final Map<String, Object> options;

    public BinaryEvaluator(Operand lhs, Operand rhs, BinaryOperator operator, Map<String, Object> options) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        this.options = options;
    }

    @Override
    public boolean evaluate() {
        return operator.test(lhs, rhs, options);
    }
}
