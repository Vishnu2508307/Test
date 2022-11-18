package com.smartsparrow.eval.operator.endswith;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class EndsWithOperator implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (options != null && options.get("IGNORE_CASE") != null &&
                "true".equalsIgnoreCase(options.get("IGNORE_CASE").toString())) {
            return ((String) lhs.getResolvedValue()).toLowerCase()
                    .endsWith(((String) rhs.getResolvedValue()).toLowerCase());
        }
        return ((String) lhs.getResolvedValue())
                .endsWith((String) rhs.getResolvedValue());
    }
}
