package com.smartsparrow.eval.operator.startswith;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class StartsWithOperator implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        if (options != null && options.get("IGNORE_CASE") != null &&
                "true".equalsIgnoreCase(options.get("IGNORE_CASE").toString())) {
            return ((String) lhs.getResolvedValue())
                    .toLowerCase()
                    .startsWith(((String) rhs.getResolvedValue()).toLowerCase());
        }
        return ((String) lhs.getResolvedValue()).startsWith((String) rhs.getResolvedValue());
    }
}
