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

class ListMutationOperationSetTest {

    @InjectMocks
    private ListMutationOperationSet listMutationOperationSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("It should return a new list with the left value in it")
    void apply() {
        List<Object> left = Lists.newArrayList("one", "two");
        List<Object> right = Lists.newArrayList("right");

        List<Object> result = listMutationOperationSet.apply(left, right);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("right", result.get(0));
    }

}
