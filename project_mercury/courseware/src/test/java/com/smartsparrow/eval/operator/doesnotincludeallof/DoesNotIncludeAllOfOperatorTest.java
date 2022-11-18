package com.smartsparrow.eval.operator.doesnotincludeallof;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class DoesNotIncludeAllOfOperatorTest {

    @InjectMocks
    DoesNotIncludeAllOfOperator doesNotIncludeAllOfOperator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void operandsAreNull() {
        Operand lhs = new Operand();
        Operand rhs = new Operand();
        assertFalse(doesNotIncludeAllOfOperator.test(lhs, rhs, null));
    }

    @Test
    void operandsAreNotListType() {
        Operand lhs = new Operand().setResolvedValue("string");
        Operand rhs = new Operand().setResolvedValue("string");

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> {
            doesNotIncludeAllOfOperator.test(lhs, rhs, null);
        });

        assertEquals("DOES_NOT_INCLUDES_ALL_OF Operation not supported for supplied operand types", e.getMessage());
    }

    @Test
    void valid_all_included() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList("string", "ing", "bling"));
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("string", "ing", "bling"));
        boolean test = doesNotIncludeAllOfOperator.test(lhs, rhs, null);
        assertFalse(test);
    }

    @Test
    void valid_doesnot_includes_any_of() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList("tring", "ig", "bing"));
        Operand rhs = new Operand().setResolvedValue(Arrays.asList("string", "ing", "bling"));
        boolean test = doesNotIncludeAllOfOperator.test(lhs, rhs, null);
        assertTrue(test);
    }
}
