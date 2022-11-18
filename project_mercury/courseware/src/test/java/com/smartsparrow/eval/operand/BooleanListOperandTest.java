package com.smartsparrow.eval.operand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class BooleanListOperandTest {

    @InjectMocks
    BooleanListOperand booleanListOperand;

    private static final List<Boolean> valueList = Arrays.asList(true, false, true);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetValueWhenListIsNull() {
        booleanListOperand = new BooleanListOperand(null);
        List<Boolean> res = booleanListOperand.getResolvedValue();
        assertNull(res);
    }

    @Test
    void testGetValueWhenListIsNonEmpty() {
        booleanListOperand = new BooleanListOperand(valueList);
        List<Boolean> res = booleanListOperand.getResolvedValue();
        assertEquals(res, valueList);
    }
}
