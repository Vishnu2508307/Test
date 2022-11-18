package com.smartsparrow.eval.operator.containsoneof;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class ContainsOneOfOperatorTest {

    @InjectMocks
    ContainsOneOfOperator containsOneOfOperator;

    @Mock
    Map<String, Object> options;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testOperationNotSupported() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("j");

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                () -> containsOneOfOperator.test(lhs, rhs, options));

        assertEquals("Operation not supported for operands", e.getMessage());
    }

    @Test
    void testWhenOneMatch() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("tes", "tea"));

        boolean result = containsOneOfOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testWhenMoreThanOneMatch() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("tes", "te"));

        boolean result = containsOneOfOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

    @Test
    void testWhenNoMatch() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("ta", "sh", "tasht"));

        boolean result = containsOneOfOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

}
