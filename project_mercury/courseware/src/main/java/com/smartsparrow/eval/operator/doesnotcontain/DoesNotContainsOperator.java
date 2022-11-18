package com.smartsparrow.eval.operator.doesnotcontain;

import java.util.Map;

import com.smartsparrow.eval.operator.contains.ContainsOperator;
import com.smartsparrow.eval.parser.Operand;

public class DoesNotContainsOperator extends ContainsOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return !super.test(lhs, rhs, options);
    }
}
