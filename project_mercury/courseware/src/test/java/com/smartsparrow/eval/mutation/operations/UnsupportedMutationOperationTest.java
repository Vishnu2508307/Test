package com.smartsparrow.eval.mutation.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class UnsupportedMutationOperationTest {

    @InjectMocks
    private UnsupportedMutationOperation unsupportedMutationOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should return the original value and apply no mutation")
    void apply() {
        String left = "left";
        String right = "right";

        Object result = unsupportedMutationOperation.apply(left, right);

        assertNotNull(result);
        assertEquals("left", result);
    }

}
