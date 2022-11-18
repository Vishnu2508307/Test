package com.smartsparrow.eval.operand;

import java.util.List;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class IntegerListOperand extends Operand {

    public IntegerListOperand(List<Integer> resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public List<Integer> getResolvedValue() {
        return (List<Integer>) super.getResolvedValue();
    }
}
