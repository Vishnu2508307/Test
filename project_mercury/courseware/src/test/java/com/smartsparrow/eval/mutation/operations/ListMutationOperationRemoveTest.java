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

class ListMutationOperationRemoveTest {

    @InjectMocks
    private ListMutationOperationRemove listMutationOperationRemove;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("it should remove the element from the list")
    void apply_remove() {
        List<Object> left = Lists.newArrayList("one", "two");
        List<Object> right = Lists.newArrayList("two");

        List<Object> result = listMutationOperationRemove.apply(left, right);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("one", result.get(0));
    }

    @Test
    @DisplayName("It should not remove the element when this is not found")
    void apply_notFound() {
        List<Object> left = Lists.newArrayList("one", "two");
        List<Object> right = Lists.newArrayList("three");

        List<Object> result = listMutationOperationRemove.apply(left, right);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

}
