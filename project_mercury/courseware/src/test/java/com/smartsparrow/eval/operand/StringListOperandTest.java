package com.smartsparrow.eval.operand;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class StringListOperandTest {

    @InjectMocks
    StringListOperand stringListOperand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNullValue() {
        stringListOperand = new StringListOperand(null);
        assertNull(stringListOperand.getResolvedValue());
    }

    @Test
    void testEmptyListValue() {
        stringListOperand = new StringListOperand(new ArrayList<>());
        assertNotNull(stringListOperand.getResolvedValue());
        assertEquals(0, stringListOperand.getResolvedValue().size());
    }

    @Test
    void testValidList() {
        List<String> nonEmptyList = Arrays.asList("a", "b", "c");
        stringListOperand = new StringListOperand(nonEmptyList);
        assertNotNull(stringListOperand.getResolvedValue());
        assertEquals(nonEmptyList, stringListOperand.getResolvedValue());
    }
}
