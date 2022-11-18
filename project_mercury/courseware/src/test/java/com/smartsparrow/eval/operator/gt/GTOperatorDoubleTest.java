package com.smartsparrow.eval.operator.gt;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class GTOperatorDoubleTest {

    @InjectMocks
    GTOperatorDouble gtOperatorDouble;

    @Mock
    Operand lhs;

    @Mock
    Operand rhs;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGETrue() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.00000001);
        boolean result = gtOperatorDouble.test(lhs, rhs, null);
        assertTrue(result);
    }

    @Test
    void testGEFalse() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.10000001);
        boolean result = gtOperatorDouble.test(lhs, rhs, null);
        assertFalse(result);
    }

    @Test
    void testEqual() {
        when(lhs.getResolvedValue()).thenReturn(12.01);
        when(rhs.getResolvedValue()).thenReturn(12.01);
        boolean result = gtOperatorDouble.test(lhs, rhs, null);
        assertFalse(result);
    }

}
