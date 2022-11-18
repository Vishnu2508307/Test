package com.smartsparrow.eval.operator.not;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.BooleanOperand;


public class NotOperatorBooleanTest {

    @InjectMocks
    NotOperatorBoolean notOperatorBoolean;

    @Mock
    BooleanOperand operand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testPositiveNegation() {
        when(operand.getResolvedValue()).thenReturn(true);
        boolean result = notOperatorBoolean.test(operand, null);
        assertFalse(result);
    }

    @Test
    void testNegativeNegation() {
        when(operand.getResolvedValue()).thenReturn(false);
        boolean result = notOperatorBoolean.test(operand, null);
        assertTrue(result);
    }
}
