package com.smartsparrow.eval.mutation.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class DifferenceMutationOperationTest {

    @InjectMocks
    private DifferenceMutationOperation differenceMutationOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("it should return the difference as a double when subtracting one double from another double")
    void apply() {
        Double left = 5d;
        Double right = 3d;

        Number result = differenceMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(2d, result);
    }

    @Test
    @DisplayName("It should return the difference as a double when subtracting one integer from a double")
    void apply_intDouble() {
        Double left = 5d;
        Integer right = 3;

        Number result = differenceMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(2d, result);
    }

    @Test
    @DisplayName("it should return the difference as a double when subtracting one double from an integer")
    void apply_doubleInteger() {
        Integer left = 5;
        Double right = 3d;

        Number result = differenceMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(2d, result);
    }

    @Test
    @DisplayName("It should return the difference as an integer when subtracting two integers")
    void apply_twoInteger() {
        Integer left = 5;
        Integer right = 3;

        Number result = differenceMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals(2, result);
    }
}
