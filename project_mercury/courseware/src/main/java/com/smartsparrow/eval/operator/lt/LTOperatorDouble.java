package com.smartsparrow.eval.operator.lt;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class LTOperatorDouble implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return ((Double) lhs.getResolvedValue()).compareTo((Double) rhs.getResolvedValue()) < 0;
    }
}
