package com.smartsparrow.eval.operand;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class BooleanOperand extends Operand {

    BooleanOperand(Boolean resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public Boolean getResolvedValue() {
        return (Boolean) super.getResolvedValue();
    }
}
