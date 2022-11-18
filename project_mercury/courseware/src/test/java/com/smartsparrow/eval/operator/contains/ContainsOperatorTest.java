package com.smartsparrow.eval.operator.contains;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class ContainsOperatorTest {

    @InjectMocks
    ContainsOperator containsOperator;

    @Mock
    Map<String, Object> options;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testWhenFalse() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("j");

        boolean result = containsOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

    @Test
    void testWhenTrue() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("test");

        boolean result = containsOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testWhenLhsIsAListAndDoesnotContain() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList("test", "tease"));
        Operand rhs = new Operand().setResolvedValue("tes");

        boolean result = containsOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

    @Test
    void testWhenLhsIsAListAndContain() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList("test", "tease", "breeze"));
        Operand rhs = new Operand().setResolvedValue("tease");

        boolean result = containsOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testWhenBothOperandsAreLists() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList("test", "tea"));
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("test", "tease"));

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            containsOperator.test(lhs, rhs, null);
        });
        assertEquals("CONTAINS Operation not supported for operand types", exception.getMessage());
    }
}
