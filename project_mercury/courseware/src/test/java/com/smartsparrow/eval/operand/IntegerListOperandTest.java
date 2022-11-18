package com.smartsparrow.eval.operand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class IntegerListOperandTest {

    @InjectMocks
    IntegerListOperand integerListOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNullValue() {
        integerListOperand = new IntegerListOperand(null);
        assertNull(integerListOperand.getResolvedValue());
    }

    @Test
    void testEmptyList() {
        List<Integer> emptyList = new ArrayList<>();
        integerListOperand = new IntegerListOperand(emptyList);
        assertNotNull(integerListOperand.getResolvedValue());
        assertEquals(0, integerListOperand.getResolvedValue().size());
    }

    @Test
    void testValidList() {
        List<Integer> validList = Arrays.asList(1, 2, 3, 4, 5);
        integerListOperand = new IntegerListOperand(validList);
        assertNotNull(validList);
        assertEquals(validList, integerListOperand.getResolvedValue());

    }
}
