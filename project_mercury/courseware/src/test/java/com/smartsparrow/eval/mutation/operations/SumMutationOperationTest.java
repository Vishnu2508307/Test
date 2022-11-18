package com.smartsparrow.eval.mutation.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class SumMutationOperationTest {

    @InjectMocks
    private SumMutationOperation sumMutationOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should return the sum as a double when adding two doubles")
    void apply_twoDouble() {
        Double left = 5d;
        Double right = 5d;

        Number result = sumMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(10d, result);
    }

    @Test
    @DisplayName("It should return the sum as a double when adding one integer to a double")
    void apply_intDouble() {
        Double left = 5d;
        Integer right = 5;

        Number result = sumMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(10d, result);
    }

    @Test
    @DisplayName("It should return the sum as an integer when adding two integers")
    void apply_twoInteger() {
        Integer left = 5;
        Integer right = 5;

        Number result = sumMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(10, result);
    }
}
