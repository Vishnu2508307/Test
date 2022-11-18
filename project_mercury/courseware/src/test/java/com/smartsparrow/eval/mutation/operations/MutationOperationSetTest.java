package com.smartsparrow.eval.mutation.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class MutationOperationSetTest {

    @InjectMocks
    private MutationOperationSet mutationOperationSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should replace the left value with the right value")
    void apply() {
        String left = "left";
        String right = "right";

        Object result = mutationOperationSet.apply(left, right);

        assertNotNull(result);
        assertEquals("right", result);
    }

}
