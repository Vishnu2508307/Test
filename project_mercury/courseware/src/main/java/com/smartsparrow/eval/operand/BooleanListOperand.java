package com.smartsparrow.eval.operand;

import java.util.List;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class BooleanListOperand extends Operand {

    public BooleanListOperand(List<Boolean> resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public List<Boolean> getResolvedValue() {
        return (List<Boolean>) super.getResolvedValue();
    }
}
