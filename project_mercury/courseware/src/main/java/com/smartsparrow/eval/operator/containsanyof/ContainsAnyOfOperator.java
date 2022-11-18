package com.smartsparrow.eval.operator.containsanyof;

import java.util.List;
import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class ContainsAnyOfOperator implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (lhs.getResolvedValue() instanceof String && rhs.getResolvedValue() instanceof List) {
            String lhsValue = (String) lhs.getResolvedValue();
            List<String> rhsValue = (List<String>) rhs.getResolvedValue();

            return rhsValue
                    .stream()
                    .anyMatch(lhsValue::contains);
        }
        throw new UnsupportedOperationException("Operation not supported for operands");
    }
}
