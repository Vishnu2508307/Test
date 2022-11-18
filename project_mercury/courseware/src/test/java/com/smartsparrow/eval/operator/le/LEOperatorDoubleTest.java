package com.smartsparrow.eval.operator.le;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.DoubleOperand;

public class LEOperatorDoubleTest {

    @InjectMocks
    LEOperatorDouble leOperatorDouble;

    @Mock
    DoubleOperand lhs;

    @Mock
    DoubleOperand rhs;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testLEFalse() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.00000001);
        boolean result = leOperatorDouble.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testLETrue() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.10000001);
        boolean result = leOperatorDouble.test(lhs, rhs, null);
        assertTrue(result);
    }

    @Test
    void testLHSEqualToRHS() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.01);
        boolean result = leOperatorDouble.test(lhs, rhs, null);
        assertTrue(result);
    }

}
