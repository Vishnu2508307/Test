package com.smartsparrow.eval.operator.le;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class LEOperatorDouble implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return ((Double) lhs.getResolvedValue()).compareTo((Double) rhs.getResolvedValue()) <= 0;
    }
}
