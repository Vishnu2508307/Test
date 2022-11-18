package com.smartsparrow.util;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.smartsparrow.exception.DateTimeFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;

class WarrantsTest {

    private static final String MSG_NOT_THROWN = "not thrown.";
    private static final String MSG_THROWN = "thrown.";

    @Test
    @DisplayName("affirm that a false condition does not throw")
    void affirmArgumentDoesNotThrow() {
        affirmArgument(true, MSG_NOT_THROWN);
    }

    @Test
    @DisplayName("affirm that a true condition does throw")
    void affirmArgumentDoesThrow() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                                                                 () -> affirmArgument(false, MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("affirm that a String that is not null or empty does not throw")
    void affirmArgumentNotNullOrEmptyDoesNotThrow() {
        affirmArgumentNotNullOrEmpty("blah", MSG_NOT_THROWN);
    }

    @Test
    @DisplayName("affirm that a String that is null does throw")
    void affirmArgumentNotNullOrEmptyDoesThrow_null() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                                                                 () -> affirmArgumentNotNullOrEmpty((String)null, MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("affirm that a String that is empty does throw")
    void affirmArgumentNotNullOrEmptyDoesThrow_empty() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                                                                 () -> affirmArgumentNotNullOrEmpty("", MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("affirm that a collection that is not null or empty does not throw")
    void affirmArgumentNotNullOrEmptyDoesNotThrow_collection() {
        affirmArgumentNotNullOrEmpty(Lists.newArrayList("1", "2"), MSG_NOT_THROWN);
    }

    @Test
    @DisplayName("affirm that a collection that is null does throw")
    void affirmArgumentNotNullOrEmptyDoesThrow_null_collection() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                () -> affirmArgumentNotNullOrEmpty((Collection) null, MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("affirm that a collection that is empty does throw")
    void affirmArgumentNotNullOrEmptyDoesThrow_empty_collection() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                () -> affirmArgumentNotNullOrEmpty(Lists.newArrayList(), MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("affirm that a real object does not throw")
    void affirmNotNullDoesNotThrow() {
        affirmNotNull(new Object(), MSG_NOT_THROWN);
    }

    @Test
    @DisplayName("affirm that null does throw")
    void affirmNotNullDoesThrow() {
        IllegalArgumentFault illegalArgumentFault = assertThrows(IllegalArgumentFault.class, //
                                                                 () -> affirmNotNull(null, MSG_THROWN));

        assertThat(illegalArgumentFault.getMessage()).isEqualTo(MSG_THROWN);
    }

    @Test
    @DisplayName("It should throw a DateTimeFault when the date cannot be formatted")
    void affirmValidDate() {
        final long futureMillis = 253415829600000L; // 06/06/20000
        DateTimeFault e = assertThrows(DateTimeFault.class, () -> Warrants.affirmValidDate(futureMillis, "invalid date time supplied"));

        assertNotNull(e);
        assertEquals("invalid date time supplied",e.getMessage());
    }


    @Test
    @DisplayName("It should throw a DateTimeFault when the date cannot be parsed")
    void affirmValidDate_string() {
        String date = "this is not a date lol";
        DateTimeFault e = assertThrows(DateTimeFault.class, () -> Warrants.affirmValidDate(date, "invalid date supplied"));

        assertNotNull(e);
        assertEquals("invalid date supplied", e.getMessage());
    }

    @Test
    @DisplayName("It should throw the fault supplied in the argument when the null check fails")
    void affirmNotNull_fault() {
        assertThrows(IllegalStateFault.class,
                () -> affirmNotNull(null, new IllegalStateFault("value cannot be null. Illegal state")));
    }
}
