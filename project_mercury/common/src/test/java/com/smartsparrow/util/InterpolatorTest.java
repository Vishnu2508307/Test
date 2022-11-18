package com.smartsparrow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InterpolatorTest {

    private static final String template = "This test is a ${result}!";

    @Test
    void interpolate_success() {
        Interpolator interpolator = new Interpolator();

        interpolator.addVariable("result", "success");

        assertEquals("This test is a success!", interpolator.interpolate(template));
    }

    @Test
    void interpolate_fail() {
        Interpolator interpolator = new Interpolator();

        assertEquals("This test is a ${result}!", interpolator.interpolate(template));
    }

}