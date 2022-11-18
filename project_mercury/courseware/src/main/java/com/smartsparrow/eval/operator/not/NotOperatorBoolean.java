package com.smartsparrow.eval.operator.not;

import java.util.Map;

import com.smartsparrow.eval.operator.UnaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class NotOperatorBoolean implements UnaryOperator {

    @Override
    public boolean test(Operand operand, Map<String, Object> options) {
        return !((Boolean) operand.getResolvedValue());
    }
}
