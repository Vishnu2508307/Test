package com.smartsparrow.util;

import static com.smartsparrow.util.DateFormat.RFC_1123;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.smartsparrow.exception.DateTimeFault;
import com.smartsparrow.exception.Fault;
import com.smartsparrow.exception.IllegalArgumentFault;

/**
 * Helper class, similar to Guava Preconditions, to check arguments and throw errors.
 *
 * This is not intended to be a replacement for Guava Preconditions; it should be used where Faults should be
 * thrown and propagated (i.e. wire and service level exposed APIs).
 *
 * define "Warrants": (verb) officially affirm or guarantee.
 */
public class Warrants {

    private static final Logger log = LoggerFactory.getLogger(Warrants.class);

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentFault if {@code expression} is false
     */
    public static void affirmArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the supplied string is not null or empty
     *
     * @param value the string to test
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentFault if {@code value} is null or empty
     */
    public static void affirmArgumentNotNullOrEmpty(final String value, Object errorMessage) {
        // this literal check works better null-checks in code analysis.
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the supplied collection is not null or empty
     *
     * @param value the collection to test
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentFault if {@code value} is null or empty
     */
    public static void affirmArgumentNotNullOrEmpty(final Collection value, Object errorMessage) {
        // this literal check works better null-checks in code analysis.
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensure that the supplied argument is not null
     *
     * @param value the value to test
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentFault if {@code value} is null or empty
     */
    public static void affirmNotNull(final Object value, Object errorMessage) {
        // this literal check works better null-checks in code analysis.
        if (value == null) {
            throw new IllegalArgumentFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Validate that the date is not too big and does not exceed the 4 digits for the year.
     *
     * I salute you developer from the future. I am from the past and at this time and year the java.time.format
     * did not support dates with more than 4 digits for the year value. I am sure this problem will be well solved
     * before it becomes an issue for you.
     *
     * @param millis the millisecond value to be formatted to RFC_1123
     * @param errorMessage the error message to use if the check fails
     * @throws DateTimeFault when there is an error formatting the date
     */
    public static void affirmValidDate(final long millis, Object errorMessage) {
        try {
            RFC_1123.format(Instant.ofEpochMilli(millis));
        } catch (DateTimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("invalid date time", e);
            }
            throw new DateTimeFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Validate that the String is a valid date.
     *
     * @param date the string representation of the date to validate
     * @param errorMessage the error message to use if the check fails
     * @throws DateTimeFault when failing to parse or format the date
     */
    public static void affirmValidDate(final String date, Object errorMessage) {
        try {
            affirmValidDate(DateFormat.fromRFC1123(date), errorMessage);
        } catch (DateTimeParseException ex) {
            if (log.isDebugEnabled()) {
                log.debug("invalid date time", ex);
            }
            throw new DateTimeFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensure that the supplied argument is not null
     *
     * @param value the value to check
     * @param fault the fault that will be thrown if the checked value is null
     */
    public static void affirmNotNull(final Object value, Fault fault) {
        // this literal check works better null-checks in code analysis.
        if (value == null) {
            throw fault;
        }
    }

    /**
     * Ensure that the supplied argument is a valid uri
     *
     * @param uri the uri to check
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentFault if the supplied argument is not a valid uri
     */
    public static void affirmValidUri(final String uri, final Object errorMessage) {
        //
        affirmArgumentNotNullOrEmpty(uri, errorMessage);
        //
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentFault(String.valueOf(errorMessage));
        }
    }

    /**
     * Check if the supplied runnable throws a {@link Throwable} and if so it throws the supplied fault
     *
     * @param runnable the runnable to test
     * @param fault the fault to throw if the runnable throws a throwable
     */
    public static void affirmDoesNotThrow(final Runnable runnable, final Fault fault) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw fault;
        }
    }
}
