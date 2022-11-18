package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class GenericsTest {

    @Test
    void parameterizedClassFor_failsWhenNoGenericInterface() {
        assertThrows(ClassNotFoundException.class, ()-> Generics.parameterizedClassFor(ParameterizedTestArgument.class));
    }

    @Test
    void parameterizedClassFor() throws ClassNotFoundException {
        Class parameterizedGeneric = Generics.parameterizedClassFor(ParameterizedTest.class);

        assertEquals(ParameterizedTestArgument.class.getTypeName(), parameterizedGeneric.getTypeName());
    }

    // Test classes
    private interface ParameterizedTestArgument {}

    private class ParameterizedTest implements Predicate<ParameterizedTestArgument> {

        @Override
        public boolean test(ParameterizedTestArgument testParameter) {
            return false;
        }
    }
}
