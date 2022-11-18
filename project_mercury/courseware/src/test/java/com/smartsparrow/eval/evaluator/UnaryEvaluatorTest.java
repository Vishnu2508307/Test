package com.smartsparrow.eval.evaluator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.operator.UnaryOperator;

public class UnaryEvaluatorTest {

    @InjectMocks
    UnaryEvaluator unaryEvaluator;

    @Mock
    UnaryOperator operator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testTrueEvaluation() {
        when(operator.test(any(), any())).thenReturn(true);
        boolean result = unaryEvaluator.evaluate();
        assertTrue(result);
    }

    @Test
    void testFalseEvaluation() {
        when(operator.test(any(), any())).thenReturn(false);
        boolean result = unaryEvaluator.evaluate();
        assertFalse(result);
    }


}
