package com.smartsparrow.eval.operator.doesnotinclude;

import java.util.List;
import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.util.OperatorUtil;
import com.smartsparrow.eval.parser.Operand;

public class DoesNotIncludeOperator implements BinaryOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (!OperatorUtil.areOperandsNull(lhs, rhs)) {
            if (OperatorUtil.isOperandOfExpectedType(lhs, List.class) &&
                    !OperatorUtil.isOperandOfExpectedType(rhs, List.class)) {

                List resolvedValue = (List) lhs.getResolvedValue();
                return !resolvedValue.contains(rhs.getResolvedValue());
            }
        } else {
            return false;
        }
        throw new UnsupportedOperationException("DOES_NOT_INCLUDE Operation not supported for operand types");
    }
}
