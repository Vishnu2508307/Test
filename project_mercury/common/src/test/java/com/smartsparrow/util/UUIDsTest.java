package com.smartsparrow.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartsparrow.exception.IllegalArgumentFault;

class UUIDsTest {

    @Test
    @DisplayName("can generate a time based uuid")
    void timeBased() {
        UUID actual = UUIDs.timeBased();
        //
        assertThat(actual, is(notNullValue()));
    }

    @Test
    @DisplayName("generated ids are version 1")
    void timeBased_v1() {
        UUID actual = UUIDs.timeBased();

        assertThat(actual.version(), is(1));
    }

    @Test
    @DisplayName("should be able to encode a timestamp")
    void timeBased_withTimestamp() {
        long ts = 42;

        UUID generated = UUIDs.timeBased(ts);

        assertThat(generated.version(), is(1));
        assertEquals(UUIDs.epochMillisOf(generated), ts);
    }

    @Test
    @DisplayName("should be able to extract a timestamp from a v1")
    void epochMillisOf() {
        UUID v1 = UUIDs.timeBased();

        assertTrue(UUIDs.epochMillisOf(v1) > 0);
    }

    @Test
    @DisplayName("should error for non v1 time extract")
    void ecpochMillisOf_wrongVersion() {
        UUID v4 = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> UUIDs.epochMillisOf(v4));
    }

    @Test
    @DisplayName("should return a value < 0 on compare")
    void compareByTime_negative() {
        UUID one = UUIDs.timeBased();
        UUID two = UUIDs.timeBased();

        assertTrue(UUIDs.compareByTime(one, two) < 0);
    }

    @Test
    @DisplayName("should return a value > 0 on compare")
    void compareByTime_positive() {
        UUID one = UUIDs.timeBased();
        UUID two = UUIDs.timeBased();

        assertTrue(UUIDs.compareByTime(two, one) > 0);
    }

    @Test
    @DisplayName("should error for non version 1 uuid")
    void compareByTime_wrongVersion() {
        UUID v1 = UUIDs.timeBased();
        UUID v4 = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> UUIDs.compareByTime(v1, v4));
        assertThrows(IllegalArgumentException.class, () -> UUIDs.compareByTime(v4, v1));
    }

    @Test
    @DisplayName("should parse a UUID from the toString() representation")
    void fromString() {
        UUID expected = UUIDs.timeBased();
        String expectedString = expected.toString();

        UUID actual = UUIDs.fromString(expectedString);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("should error on an invalid UUID from a string")
    void fromString_invalid() {
        UUID expected = UUIDs.timeBased();
        String expectedString = expected.toString() + "boom";

        assertThrows(IllegalArgumentFault.class, () -> UUIDs.fromString(expectedString));
    }
}
