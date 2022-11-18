package com.smartsparrow.eval.operator.ge;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.DoubleOperand;

public class GEOperatorDoubleTest {

    @InjectMocks
    GEOperatorDouble geOperatorDouble;

    @Mock
    DoubleOperand lhs;

    @Mock
    DoubleOperand rhs;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGETrue() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.00000001);
        boolean result = geOperatorDouble.test(lhs, rhs, null);
        assertTrue(result);
    }

    @Test
    void testGEFalse() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.10000001);
        boolean result = geOperatorDouble.test(lhs, rhs, null);
        assertFalse(result);
    }

}
