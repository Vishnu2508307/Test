package com.smartsparrow.eval.operator.doesnotcontainanyof;

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

public class DoesNotContainAnyOfOperatorTest {

    @InjectMocks
    DoesNotContainAnyOfOperator doesNotContainAnyOfOperator;

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
                () -> doesNotContainAnyOfOperator.test(lhs, rhs, options));

        assertEquals("Operation not supported for operands", e.getMessage());
    }

    @Test
    void testWhenFalse() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("tes", "te"));

        boolean result = doesNotContainAnyOfOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

    @Test
    void testWhenTrue() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("ta", "sh", "tasht"));

        boolean result = doesNotContainAnyOfOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

}
