package com.smartsparrow.eval.operator.equals;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class EqualsOperator implements BinaryOperator {

    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (lhs.getResolvedValue() instanceof List && rhs.getResolvedValue() instanceof List) {
            // isEqualCollection from Apache commons ignores order of the elements and deals with edge cases
            // where repeated items might fool a Set based approach by counting how many times items appear in the
            // collections and accounting for that in the final comparison
            return CollectionUtils.isEqualCollection((List) lhs.getResolvedValue(), (List) rhs.getResolvedValue());
        } else {
            return lhs.getResolvedValue().equals(rhs.getResolvedValue());
        }
    }
}
