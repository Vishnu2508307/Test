package com.smartsparrow.eval.evaluator;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class NegationBinaryEvaluator extends BinaryEvaluator {

    public NegationBinaryEvaluator(Operand lhs, Operand rhs, BinaryOperator operator, Map<String, Object> options) {
        super(lhs, rhs, operator, options);
    }

    @Override
    public boolean evaluate() {
        return !super.evaluate();
    }
}
