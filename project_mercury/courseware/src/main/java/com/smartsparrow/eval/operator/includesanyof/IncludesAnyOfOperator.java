package com.smartsparrow.eval.operator.includesanyof;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.util.OperatorUtil;
import com.smartsparrow.eval.parser.Operand;

public class IncludesAnyOfOperator implements BinaryOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (!OperatorUtil.areOperandsNull(lhs, rhs)) {
            if (OperatorUtil.areOperandsListType(lhs, rhs)) {
                return ListUtils.intersection((List) lhs.getResolvedValue(), (List) rhs.getResolvedValue()).size() >= 1;
            }
            throw new UnsupportedOperationException("INCLUDES_ANY_OF Operation not supported for supplied operand types");
        } else {
            return false;
        }
    }
}
