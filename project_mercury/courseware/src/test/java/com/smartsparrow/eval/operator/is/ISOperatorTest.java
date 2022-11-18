package com.smartsparrow.eval.operator.is;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.eval.parser.Operand;

public class ISOperatorTest {

    @InjectMocks
    ISOperator isOperator;

    @Mock
    Operand lhs;

    @Mock
    Operand rhs;

    @Mock
    Map<String, Object> options;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        isOperator = new ISOperator();
    }

    @Test
    void testFalseLists_DifferentSizeList_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("test", "starts", "now"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("now"));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_sameOrder_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("a", "b", "c", "d"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("a", "b", "c", "d"));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_diffOrder_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("a", "b", "c", "d"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("a", "c", "b", "d"));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_diffOrderWithRepeats_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("a", "a", "b", "c", "d"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("a", "c", "b", "d", "a"));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_diffOrderWithUnevenRepeats_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("a", "a", "b", "b", "c", "d"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("a", "a", "a", "b", "d", "c"));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_String() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList("test", "starts", "now"));
        when(rhs.getResolvedValue()).thenReturn(newArrayList("tst", "strts", "nw"));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_DifferentSizeList_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(true, false, true));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(true, false));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(true, false, true));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(true, false, true));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_DiffOrder_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(true, false, true));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(true, true, false));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(true, true, false));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(true, false, false));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_DifferentSizeList_Double() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(1d, 2d, 3d));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(2d, 3d));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_DiffOrder_Double() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(1d, 2d, 3d));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(1d, 3d, 2d));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrueLists_Double() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(1d, 2d, 3d));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(1d, 2d, 3d));
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalseLists_Double() {
        when(lhs.getResolvedValue()).thenReturn(newArrayList(1d, 1d, 2d, 3d));
        when(rhs.getResolvedValue()).thenReturn(newArrayList(1d, 2d, 2d, 3d));
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrue_String() {
        when(lhs.getResolvedValue()).thenReturn("test");
        when(rhs.getResolvedValue()).thenReturn("test");
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalse_String() {
        when(lhs.getResolvedValue()).thenReturn("2.30");
        when(rhs.getResolvedValue()).thenReturn("2.301");
        assertFalse(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testTrue_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(true);
        assertTrue(isOperator.test(lhs, rhs, options));
    }

    @Test
    void testFalse_Boolean() {
        when(lhs.getResolvedValue()).thenReturn(true);
        when(rhs.getResolvedValue()).thenReturn(false);
        assertFalse(isOperator.test(lhs, rhs, options));
    }

}
