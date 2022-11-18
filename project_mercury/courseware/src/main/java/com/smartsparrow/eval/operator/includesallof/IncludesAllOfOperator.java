package com.smartsparrow.eval.operator.includesallof;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.util.OperatorUtil;
import com.smartsparrow.eval.parser.Operand;

public class IncludesAllOfOperator implements BinaryOperator {
    @SuppressWarnings("unchecked")
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (!OperatorUtil.areOperandsNull(lhs, rhs)) {
            if (OperatorUtil.areOperandsListType(lhs, rhs)) {
                Collection lhsResolvedValue = (List) lhs.getResolvedValue();
                Collection rhsResolvedValue = (List) rhs.getResolvedValue();
                return lhsResolvedValue.containsAll(rhsResolvedValue);
            }
        } else {
            return false;
        }
        throw new UnsupportedOperationException("INCLUDES_ALL_OF Operation not supported for supplied operand types");
    }


}
