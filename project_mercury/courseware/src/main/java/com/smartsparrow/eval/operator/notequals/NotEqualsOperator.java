package com.smartsparrow.eval.operator.notequals;

import java.util.Map;

import com.smartsparrow.eval.operator.equals.EqualsOperator;
import com.smartsparrow.eval.parser.Operand;

public class NotEqualsOperator extends EqualsOperator {
    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return !super.test(lhs, rhs, options);
    }
}
