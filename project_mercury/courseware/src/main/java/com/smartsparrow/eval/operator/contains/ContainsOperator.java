package com.smartsparrow.eval.operator.contains;

import java.util.List;
import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.util.OperatorUtil;
import com.smartsparrow.eval.parser.Operand;

public class ContainsOperator implements BinaryOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (OperatorUtil.areOperandsNull(lhs, rhs)) {
            return false;
        }
        if (OperatorUtil.areOperandsStringType(lhs, rhs)) {
            return ((String) lhs.getResolvedValue()).contains((String) rhs.getResolvedValue());
        }
        if (OperatorUtil.isOperandOfExpectedType(lhs, List.class) && !OperatorUtil.isOperandOfExpectedType(rhs, List.class)) {
            List leftValue = (List) lhs.getResolvedValue();
            return leftValue.contains(rhs.getResolvedValue());
        }
        throw new UnsupportedOperationException("CONTAINS Operation not supported for operand types");
    }
}
