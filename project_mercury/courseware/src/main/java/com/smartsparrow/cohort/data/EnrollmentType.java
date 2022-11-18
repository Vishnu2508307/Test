package com.smartsparrow.cohort.data;

import com.google.common.collect.Lists;

public enum EnrollmentType {

    @Deprecated // should be replaced with TRUSTED_AUTHENTICATION instead
    LMS,

    @Deprecated // should be relaced with INSTRUCTOR instead
    MANUAL,

    // TODO replace with a better name
    SELF,

    TRUSTED_AUTHENTICATION,

    INSTRUCTOR,

    @Deprecated // payment handled by external service
    PAYMENT,

    @Deprecated // payment handled by external service
    ALREADY_PAID,

    @Deprecated // payment handled by external service
    FREE_TRIAL,

    EXTENSION,

    UNENROLLED,

    // check that the user has product entitlement before enrollment
    PASSPORT,

    // allows any user to enroll
    OPEN,

    LTI;

    /**
     * Check whether the user is allowed to set this enrollment type to the cohort
     *
     * @param enrollmentType the enrollment type to check the validity for
     * @return <code>true</code> when the type is any of:
     * <br> [{@link EnrollmentType#PASSPORT}, {@link EnrollmentType#OPEN}]
     * <br><code>false</code> when any other enrollment type is supplied
     */
    public static boolean allowedFromUser(final EnrollmentType enrollmentType) {
        return Lists.newArrayList(PASSPORT, OPEN, LTI)
                .stream()
                .anyMatch(next -> next.equals(enrollmentType));
    }
}
