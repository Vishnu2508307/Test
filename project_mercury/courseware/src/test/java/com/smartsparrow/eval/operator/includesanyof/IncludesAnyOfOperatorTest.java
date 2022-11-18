package com.smartsparrow.eval.operator.includesanyof;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class IncludesAnyOfOperatorTest {

    @InjectMocks
    IncludesAnyOfOperator includesAnyOfOperator;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void operandsAreNull() {
        Operand lhs = new Operand();
        Operand rhs = new Operand();
        assertFalse(includesAnyOfOperator.test(lhs, rhs, null));
    }

    @Test
    void operandsAreNotListType() {
        Operand lhs = new Operand().setResolvedValue("test");
        Operand rhs = new Operand().setResolvedValue("test");
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> includesAnyOfOperator
                .test(lhs, rhs, null));
        assertEquals("INCLUDES_ANY_OF Operation not supported for supplied operand types", e.getMessage());
    }

    @Test
    void valid() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList(1, 2, 4, 5));
        Operand rhs = new Operand().setResolvedValue(Arrays.asList(5, 2, 4, 1));
        boolean test = includesAnyOfOperator.test(lhs, rhs, null);
        assertTrue(test);
    }

    @Test
    void invalid() {
        Operand lhs = new Operand().setResolvedValue(Arrays.asList( 21, 14, 15));
        Operand rhs = new Operand().setResolvedValue(Arrays.asList(5, 2, 4, 1, 1));
        boolean test = includesAnyOfOperator.test(lhs, rhs, null);
        assertFalse(test);
    }
}
