package com.smartsparrow.cohort.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class CohortEnrollment {

    private UUID accountId;
    private UUID cohortId;
    private EnrollmentType enrollmentType;
    private UUID enrollmentDate;
    private String enrolledAt;
    private Integer ttlSeconds;
    private String expiresAt;
    private UUID enrolledBy;
    private String pearsonUid;

    public UUID getAccountId() {
        return accountId;
    }

    public CohortEnrollment setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortEnrollment setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public CohortEnrollment setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
        return this;
    }

    public UUID getEnrollmentDate() {
        return enrollmentDate;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public CohortEnrollment setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
        return this;
    }

    public CohortEnrollment setEnrollmentDate(UUID enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
        return this;
    }

    @GraphQLIgnore
    @JsonIgnore
    @Nullable
    public Integer getTtlSeconds() {
        return ttlSeconds;
    }

    public CohortEnrollment setTtlSeconds(Integer ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    @Nullable
    public String getExpiresAt() {
        return expiresAt;
    }

    public CohortEnrollment setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Nullable
    public UUID getEnrolledBy() {
        return enrolledBy;
    }

    public CohortEnrollment setEnrolledBy(UUID enrolledBy) {
        this.enrolledBy = enrolledBy;
        return this;
    }

    public String getPearsonUid() {
        return pearsonUid;
    }

    public CohortEnrollment setPearsonUid(String pearsonUid) {
        this.pearsonUid = pearsonUid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortEnrollment that = (CohortEnrollment) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(cohortId, that.cohortId) &&
                enrollmentType == that.enrollmentType &&
                Objects.equals(enrollmentDate, that.enrollmentDate) &&
                Objects.equals(enrolledAt, that.enrolledAt) &&
                Objects.equals(ttlSeconds, that.ttlSeconds) &&
                Objects.equals(expiresAt, that.expiresAt) &&
                Objects.equals(enrolledBy, that.enrolledBy) &&
                Objects.equals(pearsonUid, that.pearsonUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, cohortId, enrollmentType, enrollmentDate, enrolledAt,
                ttlSeconds, expiresAt, enrolledBy, pearsonUid);
    }

    @Override
    public String toString() {
        return "CohortEnrollment{" +
                "accountId=" + accountId +
                ", cohortId=" + cohortId +
                ", enrollmentType=" + enrollmentType +
                ", enrollmentDate=" + enrollmentDate +
                ", enrolledAt='" + enrolledAt + '\'' +
                ", ttlSeconds=" + ttlSeconds +
                ", expiresAt='" + expiresAt + '\'' +
                ", enrolledBy=" + enrolledBy +
                ", pearsonUid='" + pearsonUid + '\'' +
                '}';
    }
}
