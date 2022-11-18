package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

public class HistoricalCohortEnrollment extends CohortEnrollment {

    private EnrollmentStatus enrollmentStatus;

    public EnrollmentStatus getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public HistoricalCohortEnrollment setEnrollmentStatus(final EnrollmentStatus enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setAccountId(final UUID accountId) {
        super.setAccountId(accountId);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setCohortId(final UUID cohortId) {
        super.setCohortId(cohortId);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setEnrollmentType(final EnrollmentType enrollmentType) {
        super.setEnrollmentType(enrollmentType);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setEnrolledAt(final String enrolledAt) {
        super.setEnrolledAt(enrolledAt);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setEnrollmentDate(final UUID enrollmentDate) {
        super.setEnrollmentDate(enrollmentDate);
        return this;
    }


    @Override
    public HistoricalCohortEnrollment setTtlSeconds(final Integer ttlSeconds) {
        super.setTtlSeconds(ttlSeconds);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setExpiresAt(final String expiresAt) {
        super.setExpiresAt(expiresAt);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setEnrolledBy(final UUID enrolledBy) {
        super.setEnrolledBy(enrolledBy);
        return this;
    }

    @Override
    public HistoricalCohortEnrollment setPearsonUid(String pearsonUid) {
        super.setPearsonUid(pearsonUid);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HistoricalCohortEnrollment that = (HistoricalCohortEnrollment) o;
        return enrollmentStatus == that.enrollmentStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enrollmentStatus);
    }

    @Override
    public String toString() {
        return "HistoricalCohortEnrollment{" +
                "enrollmentStatus=" + enrollmentStatus +
                "} " + super.toString();
    }
}
