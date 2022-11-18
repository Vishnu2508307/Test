package com.smartsparrow.eval.operator.isnot;

import java.util.Map;

import com.smartsparrow.eval.operator.is.ISOperator;
import com.smartsparrow.eval.parser.Operand;

@Deprecated
/**
 * Use {@link com.smartsparrow.eval.operator.notequals.NotEqualsOperator}
 */
public class IsNotOperator extends ISOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return !super.test(lhs, rhs, options);
    }
}
