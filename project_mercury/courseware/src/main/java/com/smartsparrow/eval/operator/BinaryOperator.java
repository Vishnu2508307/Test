package com.smartsparrow.eval.operator;

import java.util.Map;

import com.smartsparrow.eval.parser.Operand;

public interface BinaryOperator extends Operator {

    boolean test(Operand lhs, Operand rhs, Map<String, Object> options);

}
