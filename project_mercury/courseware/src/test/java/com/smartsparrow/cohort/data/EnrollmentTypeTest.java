package com.smartsparrow.cohort.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class EnrollmentTypeTest {

    @Test
    void allowedFromUser() {

        assertTrue(EnrollmentType.allowedFromUser(EnrollmentType.OPEN));
        assertTrue(EnrollmentType.allowedFromUser(EnrollmentType.PASSPORT));
        assertTrue(EnrollmentType.allowedFromUser(EnrollmentType.LTI));
        Arrays.stream(EnrollmentType.values())
                .filter(one -> !one.equals(EnrollmentType.OPEN))
                .filter(one -> !one.equals(EnrollmentType.PASSPORT))
                .filter(one -> !one.equals(EnrollmentType.LTI))
                .collect(Collectors.toList())
                .forEach(current -> {
                    assertFalse(EnrollmentType.allowedFromUser(current));
                });
    }

}