package com.smartsparrow.eval.operator.or;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.BooleanOperand;

public class OrOperatorBooleanTest {

    @InjectMocks
    OrOperatorBoolean orOperatorBoolean;

    @Mock
    BooleanOperand lhs;

    @Mock
    BooleanOperand rhs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFalseOrFalse() {
        when(lhs.getResolvedValue()).thenReturn(false);
        when(rhs.getResolvedValue()).thenReturn(false);
        boolean result = orOperatorBoolean.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testFalseOrTrue() {
        when(lhs.getResolvedValue()).thenReturn(false);
        when(rhs.getResolvedValue()).thenReturn(true);
        boolean result = orOperatorBoolean.test(lhs, rhs, null);
        assertTrue(result);
    }

    @Test
    void testTrueOrFalse() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(false);
        boolean result = orOperatorBoolean.test(lhs, rhs, null);
        assertTrue(result);
    }

    @Test
    void testTrueOrTrue() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(true);
        boolean result = orOperatorBoolean.test(lhs, rhs, null);
        assertTrue(result);
    }

}
