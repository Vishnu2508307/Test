package com.smartsparrow.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RandomStringsTest {

    private final static String POOL = "abcdefg";

    @Test
    public void generate() {
        String randomStr = RandomStrings.random(5, POOL);

        assertThat(randomStr, notNullValue());
        assertThat(5, is(randomStr.length()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void generate_notEmptyPool() {
        RandomStrings.random(5, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generate_notNullPool() {
        RandomStrings.random(5, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generate_ensurePositiveLength() {
        RandomStrings.random(-5, POOL);
    }
}
