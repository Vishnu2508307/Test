package com.smartsparrow.eval.operator.doesnotcontain;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class DoesNotContainsOperatorTest {

    @InjectMocks
    DoesNotContainsOperator doesNotContainsOperator;

    @Mock
    Map<String, Object> options;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testWhenTrue() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("j");

        boolean result = doesNotContainsOperator.test(lhs, rhs, options);
        assertTrue(result);
    }

    @Test
    void testWhenFalse() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("tes");

        boolean result = doesNotContainsOperator.test(lhs, rhs, options);
        assertFalse(result);
    }
}
