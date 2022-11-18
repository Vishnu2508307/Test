package com.smartsparrow.eval.operand;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class StringOperand extends Operand {

    StringOperand(String resolvedValue){
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public String getResolvedValue() {
        return (String) super.getResolvedValue();
    }
}
