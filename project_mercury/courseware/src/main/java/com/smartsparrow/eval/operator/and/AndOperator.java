package com.smartsparrow.eval.operator.and;

import java.util.Map;

import com.smartsparrow.eval.operator.BinaryOperator;
import com.smartsparrow.eval.parser.Operand;

public class AndOperator implements BinaryOperator {

    @Override
    public boolean test(Operand obj1, Operand obj2, Map<String, Object> options) {
        //here later we can fetch type from Operand and have one more argument operandType
        if (obj1.getResolvedValue() instanceof Boolean && obj2.getResolvedValue() instanceof Boolean) {
            return Boolean.logicalAnd((Boolean) obj1.getResolvedValue(), (Boolean) obj2.getResolvedValue());
        }
        throw new UnsupportedOperationException("only boolean supported");
    }

}
