package com.smartsparrow.eval.operand;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class StringOperandTest {

    @InjectMocks
    StringOperand stringOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNullValue() {
        stringOperand = new StringOperand(null);
        assertNull(stringOperand.getResolvedValue());
    }

    @Test
    void testValidStringOperand() {
        stringOperand = new StringOperand("test");
        assertNotNull(stringOperand.getResolvedValue());
        assertEquals("test", stringOperand.getResolvedValue());
    }

}
