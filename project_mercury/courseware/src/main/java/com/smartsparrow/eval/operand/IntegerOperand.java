package com.smartsparrow.eval.operand;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class IntegerOperand extends Operand {

    IntegerOperand(Integer resolvedValue){
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public Integer getResolvedValue() {
        return (Integer) super.getResolvedValue();
    }
}
