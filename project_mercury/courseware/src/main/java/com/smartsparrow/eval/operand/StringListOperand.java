package com.smartsparrow.eval.operand;

import java.util.List;

import com.smartsparrow.eval.parser.Operand;

@Deprecated
public class StringListOperand extends Operand {

    StringListOperand(List<String> resolvedValue) {
        super.setResolvedValue(resolvedValue);
    }

    @Override
    public List<String> getResolvedValue() {
        return (List<String>) super.getResolvedValue();
    }
}
