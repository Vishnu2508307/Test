package com.smartsparrow.eval.mutation.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

class ListMutationOperationAddTest {

    @InjectMocks
    private ListMutationOperationAdd listMutationOperationAdd;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should add the right element to the left list")
    void apply() {
        List<Object> left = Lists.newArrayList("one", "two");
        List<Object> right = Lists.newArrayList("three");

        List<Object> result = listMutationOperationAdd.apply(left, right);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("three", result.get(2));
    }
}
