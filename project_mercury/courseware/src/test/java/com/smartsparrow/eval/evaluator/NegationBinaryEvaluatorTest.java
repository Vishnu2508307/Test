package com.smartsparrow.eval.evaluator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operator.BinaryOperator;

public class NegationBinaryEvaluatorTest {

    @InjectMocks
    NegationBinaryEvaluator negationBinaryEvaluator;

    @Mock
    BinaryOperator operator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testTrueEvaluation() {
        when(operator.test(any(), any(), any())).thenReturn(false);
        boolean result = negationBinaryEvaluator.evaluate();
        assertTrue(result);
    }

    @Test
    void testFalseEvaluation() {
        when(operator.test(any(), any(), any())).thenReturn(true);
        boolean result = negationBinaryEvaluator.evaluate();
        assertFalse(result);
    }

}
