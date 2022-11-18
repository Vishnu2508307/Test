package com.smartsparrow.eval.operator.doesnotcontainanyof;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class DoesNotContainAnyOfOperator implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (lhs.getResolvedValue() instanceof String && rhs.getResolvedValue() instanceof List<?>) {
            String lhsValue = (String) lhs.getResolvedValue();
            List<String> rhsValue = (List<String>) rhs.getResolvedValue();

            return rhsValue
                    .stream()
                    .filter(lhsValue::contains)
                    .collect(Collectors.toList()).size() == 0;

        }
        throw new UnsupportedOperationException("Operation not supported for operands");
    }
}
