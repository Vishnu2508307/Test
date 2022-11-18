package com.smartsparrow.eval.operand;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class IntegerOperandTest {

    @InjectMocks
    IntegerOperand integerOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNull() {
        integerOperand = new IntegerOperand(null);
        assertNull(integerOperand.getResolvedValue());
    }

    @Test
    void testValidIntegerOperand() {
        integerOperand = new IntegerOperand(2);
        assertNotNull(integerOperand.getResolvedValue());
        assertEquals(2, integerOperand.getResolvedValue().intValue());
    }


}
