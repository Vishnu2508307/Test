package com.smartsparrow.eval.operator.startswith;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operand.StringOperand;

public class StartsWithOperatorTest {

    @InjectMocks
    StartsWithOperator startsWithOperator;

    @Mock
    StringOperand lhs;

    @Mock
    StringOperand rhs;

    @Mock
    Map<String, Object> options;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testTrueCase() {
        when(lhs.getResolvedValue()).thenReturn("TESTING");
        when(rhs.getResolvedValue()).thenReturn("TEST");

        boolean result = startsWithOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testFalseCase() {
        when(lhs.getResolvedValue()).thenReturn("TESTING");
        when(rhs.getResolvedValue()).thenReturn("PES");

        boolean result = startsWithOperator.test(lhs, rhs, options);
        assertFalse(result);
    }

    @Test
    void testTrueWithIgnoreCase() {
        when(lhs.getResolvedValue()).thenReturn("testing");
        when(rhs.getResolvedValue()).thenReturn("TEST");
        when(options.get("IGNORE_CASE")).thenReturn("true");

        boolean result = startsWithOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testFalseWithIgnoreCase() {
        when(lhs.getResolvedValue()).thenReturn("!esting");
        when(rhs.getResolvedValue()).thenReturn("!EA");
        when(options.get("IGNORE_CASE")).thenReturn("true");

        boolean result = startsWithOperator.test(lhs, rhs, options);
        assertFalse(result);
    }
}
