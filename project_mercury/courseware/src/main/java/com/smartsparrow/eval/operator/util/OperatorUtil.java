package com.smartsparrow.eval.operator.util;

import java.util.List;

import com.smartsparrow.eval.parser.Operand;

public class OperatorUtil {

    public static boolean areOperandsNull(Operand lhs, Operand rhs) {
        return lhs.getResolvedValue() == null || rhs.getResolvedValue() == null;
    }

    public static boolean areOperandsListType(Operand lhs, Operand rhs) {
        return isOperandOfExpectedType(lhs, List.class)
                && isOperandOfExpectedType(rhs, List.class);
    }

    public static boolean areOperandsStringType(Operand lhs, Operand rhs) {
        return isOperandOfExpectedType(lhs, String.class)
                && isOperandOfExpectedType(rhs, String.class);
    }

    public static boolean areOperandsBooleanType(Operand lhs, Operand rhs) {
        return isOperandOfExpectedType(lhs, Boolean.class)
                && isOperandOfExpectedType(rhs, Boolean.class);
    }

    public static boolean isOperandOfExpectedType(Operand operand, Class clazz){
        return clazz.isInstance(operand.getResolvedValue());

    }
}
