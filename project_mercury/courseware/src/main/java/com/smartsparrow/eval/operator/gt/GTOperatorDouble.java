package com.smartsparrow.eval.operator.gt;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class GTOperatorDouble implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return ((Double) lhs.getResolvedValue()).compareTo((Double) rhs.getResolvedValue()) > 0;
    }
}
