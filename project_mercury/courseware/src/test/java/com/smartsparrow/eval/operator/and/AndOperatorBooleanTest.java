package com.smartsparrow.eval.operator.and;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.BooleanOperand;

public class AndOperatorBooleanTest {

    @InjectMocks
    AndOperator andOperator;

    @Mock
    BooleanOperand lhs;

    @Mock
    BooleanOperand rhs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFalseAndFalse() {
        when(lhs.getResolvedValue()).thenReturn(false);
        when(rhs.getResolvedValue()).thenReturn(false);
        boolean result = andOperator.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testFalseAndTrue() {
        when(lhs.getResolvedValue()).thenReturn(false);
        when(rhs.getResolvedValue()).thenReturn(true);
        boolean result = andOperator.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testTrueAndFalse() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(false);
        boolean result = andOperator.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testTrueAndTrue() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(true);
        boolean result = andOperator.test(lhs, rhs, null);
        assertTrue(result);
    }

}
