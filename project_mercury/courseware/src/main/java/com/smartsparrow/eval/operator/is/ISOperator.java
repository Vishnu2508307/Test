package com.smartsparrow.eval.operator.is;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.operator.equals.EqualsOperator;
import com.smartsparrow.eval.parser.Operand;

@Deprecated
/**
 * Use {@link EqualsOperator}
 *
 * This operator fell in disuse due to confusion with equals and now is just an alias to it.
 */
public class ISOperator implements BinaryOperator {

    @Override
    public boolean test(Operand lhs, Operand rhs, Map<String, Object> options) {
        return new EqualsOperator().test(lhs, rhs, options);
    }
}
