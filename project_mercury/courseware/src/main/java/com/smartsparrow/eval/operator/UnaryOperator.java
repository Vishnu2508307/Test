package com.smartsparrow.eval.operator;

import java.util.Map;

import com.smartsparrow.eval.parser.Operand;

public interface UnaryOperator extends Operator {

    boolean test(Operand operand, Map<String, Object> options);
}
