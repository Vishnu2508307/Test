package com.smartsparrow.eval.operand;

import java.util.List;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class DoubleListOperand extends Operand {

    DoubleListOperand(List<Double> resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public List<Double> getResolvedValue() {
        return (List<Double>) super.getResolvedValue();
    }
}
