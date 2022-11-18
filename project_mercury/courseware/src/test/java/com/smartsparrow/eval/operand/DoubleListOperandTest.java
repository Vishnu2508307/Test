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

public class DoubleListOperandTest {

    @InjectMocks
    DoubleListOperand doubleListOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNullList() {
        doubleListOperand = new DoubleListOperand(null);
        assertNull(doubleListOperand.getResolvedValue());
    }

    @Test
    void testEmptyList() {
        List<Double> emptyList = new ArrayList<>();
        doubleListOperand = new DoubleListOperand(emptyList);
        assertNotNull(doubleListOperand.getResolvedValue());
        assertEquals(emptyList, doubleListOperand.getResolvedValue());
    }

    @Test
    void testValidList() {
        List<Double> nonEmptyList = Arrays.asList(2.1d, 3.1d);
        doubleListOperand = new DoubleListOperand(nonEmptyList);
        assertEquals(nonEmptyList, doubleListOperand.getResolvedValue());
    }
}
