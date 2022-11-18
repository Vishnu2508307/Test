package com.smartsparrow.eval.evaluator;

import java.util.Map;

import com.smartsparrow.eval.operator.UnaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class NegationUnaryEvaluator extends UnaryEvaluator {


    public NegationUnaryEvaluator(Operand lhs, UnaryOperator operator, Map<String, Object> options) {
        super(lhs, operator, options);
    }

    @Override
    public boolean evaluate() {
        return !super.evaluate();
    }
}
