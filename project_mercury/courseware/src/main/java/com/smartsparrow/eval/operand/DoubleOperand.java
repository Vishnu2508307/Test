package com.smartsparrow.eval.operand;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class DoubleOperand extends Operand {

    DoubleOperand(Double resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public Double getResolvedValue() {
        return (Double) super.getResolvedValue();
    }
}
