package com.smartsparrow.eval.operand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class DoubleOperandTest {

    @InjectMocks
    DoubleOperand doubleOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNullValue() {
        doubleOperand = new DoubleOperand(null);
        assertNull(doubleOperand.getResolvedValue());
    }

    @Test
    void testValidDoubleValue() {
        doubleOperand = new DoubleOperand(1.2D);
        assertEquals(1.2D, doubleOperand.getResolvedValue().doubleValue());
    }

}
