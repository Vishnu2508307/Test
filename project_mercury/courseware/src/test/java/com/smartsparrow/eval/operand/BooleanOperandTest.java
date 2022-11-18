package com.smartsparrow.eval.operand;


import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class BooleanOperandTest {

    @InjectMocks
    BooleanOperand booleanOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testNullValue() {
        booleanOperand = new BooleanOperand(null);
        assertNull(booleanOperand.getResolvedValue());
    }

    @Test
    void testTrueValue() {
        booleanOperand = new BooleanOperand(true);
        assertTrue(booleanOperand.getResolvedValue());
    }

    @Test
    void testFalseValue() {
        booleanOperand = new BooleanOperand(false);
        assertFalse(booleanOperand.getResolvedValue());
    }
}
