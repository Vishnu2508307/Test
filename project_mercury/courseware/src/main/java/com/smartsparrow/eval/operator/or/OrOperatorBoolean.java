package com.smartsparrow.eval.operator.or;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class OrOperatorBoolean implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return Boolean.logicalOr((Boolean) lhs.getResolvedValue(), (Boolean) rhs.getResolvedValue());
    }
}
