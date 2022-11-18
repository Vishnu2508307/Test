package com.smartsparrow.eval.operator.doesnotincludeanyof;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.util.OperatorUtil;
import com.smartsparrow.eval.parser.Operand;

public class DoesNotIncludeAnyOfOperator implements BinaryOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (!OperatorUtil.areOperandsNull(lhs, rhs)) {
            if (OperatorUtil.areOperandsListType(lhs, rhs)) {
                return ListUtils
                        .intersection((List) lhs.getResolvedValue(), (List) rhs.getResolvedValue())
                        .size() == 0;
            }
            throw new UnsupportedOperationException("DOES_NOT_INCLUDES_ANY_OF Operation not supported for supplied operand types");
        } else {
            return false;
        }
    }
}
