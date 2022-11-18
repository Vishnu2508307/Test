package com.smartsparrow.eval.operator.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.smartsparrow.eval.parser.Operand;

class OperatorUtilTest {

    @Test
    void areOperandsNull() {
        //noinspection ConstantConditions
        Operand nonNullVal = new Operand().setResolvedValue("ohai");
        Operand nullVal = new Operand();
        assertTrue(OperatorUtil.areOperandsNull(nullVal, nonNullVal));
        assertTrue(OperatorUtil.areOperandsNull(nonNullVal, nullVal));
        assertFalse(OperatorUtil.areOperandsNull(nonNullVal, nonNullVal));
        assertTrue(OperatorUtil.areOperandsNull(nullVal, nullVal));
    }

    @Test
    void areOperandsListType() {
        Operand listType = new Operand().setResolvedValue(new ArrayList<>());
        Operand nonListType = new Operand().setResolvedValue("ohai");
        assertFalse(OperatorUtil.areOperandsListType(listType, nonListType));
        assertFalse(OperatorUtil.areOperandsListType(nonListType, listType));
        assertFalse(OperatorUtil.areOperandsListType(nonListType, nonListType));
        assertTrue(OperatorUtil.areOperandsListType(listType, listType));
    }

    @Test
    void areOperandsStringType() {
        Operand stringType = new Operand().setResolvedValue("ohai");
        Operand nonStringType = new Operand().setResolvedValue(1);
        assertFalse(OperatorUtil.areOperandsStringType(stringType, nonStringType));
        assertFalse(OperatorUtil.areOperandsStringType(nonStringType, stringType));
        assertFalse(OperatorUtil.areOperandsStringType(nonStringType, nonStringType));
        assertTrue(OperatorUtil.areOperandsStringType(stringType, stringType));
    }

    @Test
    void areOperandsBooleanType() {
        Operand booleanType = new Operand().setResolvedValue(true);
        Operand nonBooleanType = new Operand().setResolvedValue("nope");
        assertFalse(OperatorUtil.areOperandsBooleanType(booleanType, nonBooleanType));
        assertFalse(OperatorUtil.areOperandsBooleanType(nonBooleanType, booleanType));
        assertFalse(OperatorUtil.areOperandsBooleanType(nonBooleanType, nonBooleanType));
        assertTrue(OperatorUtil.areOperandsBooleanType(booleanType, booleanType));
    }

    @Test
    void isOperandOfExpectedType() {
        Operand operand = new Operand().setResolvedValue(1);
        assertFalse(OperatorUtil.isOperandOfExpectedType(operand, String.class));
        assertTrue(OperatorUtil.isOperandOfExpectedType(operand, Integer.class));
    }
}
