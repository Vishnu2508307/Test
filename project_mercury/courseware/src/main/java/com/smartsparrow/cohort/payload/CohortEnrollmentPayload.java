package com.smartsparrow.cohort.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.util.DateFormat;

public class CohortEnrollmentPayload {

    private final static Logger log = LoggerFactory.getLogger(CohortEnrollmentPayload.class);

    private UUID cohortId;
    private String enrolledAt;
    private EnrollmentType enrollmentType;
    private AccountSummaryPayload accountSummaryPayload;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortEnrollmentPayload setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public CohortEnrollmentPayload setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
        return this;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public CohortEnrollmentPayload setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
        return this;
    }

    @JsonProperty("accountSummary")
    public AccountSummaryPayload getAccountSummaryPayload() {
        return accountSummaryPayload;
    }

    public CohortEnrollmentPayload setAccountSummaryPayload(AccountSummaryPayload accountSummaryPayload) {
        this.accountSummaryPayload = accountSummaryPayload;
        return this;
    }

    /**
     * Helper method to create a cohort enrollment payload. The method replaces the identity account id when not defined
     * with the account id from the cohort enrollment object.
     * 
     * @param cohortEnrollment the cohort enrollment object
     * @param identity the account identity attributes object
     * @param avatar the account avatar object
     * @return a {@link CohortEnrollmentPayload}
     */
    public static CohortEnrollmentPayload from(@Nonnull CohortEnrollment cohortEnrollment,
                                               @Nonnull AccountIdentityAttributes identity,
                                               @Nonnull AccountAvatar avatar) {
        if (identity.getAccountId() == null) {
            log.warn("could not find identity for account {} while mapping a cohort enrollment payload",
                    cohortEnrollment.getAccountId());
            identity.setAccountId(cohortEnrollment.getAccountId());
        }

        return new CohortEnrollmentPayload()
                .setCohortId(cohortEnrollment.getCohortId())
                .setEnrollmentType(cohortEnrollment.getEnrollmentType())
                .setEnrolledAt(DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()))
                .setAccountSummaryPayload(AccountSummaryPayload.from(identity, avatar));
    }

    /**
     * Create a cohort enrollment payload.
     *
     * @param cohortEnrollment the cohort enrollment
     * @param accountSummaryPayload the account summary payload
     * @return a {@link CohortEnrollmentPayload} object
     */
    public static CohortEnrollmentPayload from(@Nonnull CohortEnrollment cohortEnrollment, @Nonnull AccountSummaryPayload accountSummaryPayload) {
        return new CohortEnrollmentPayload()
                .setCohortId(cohortEnrollment.getCohortId())
                .setEnrollmentType(cohortEnrollment.getEnrollmentType())
                .setEnrolledAt(DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()))
                .setAccountSummaryPayload(accountSummaryPayload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortEnrollmentPayload that = (CohortEnrollmentPayload) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(enrolledAt, that.enrolledAt) &&
                enrollmentType == that.enrollmentType &&
                Objects.equals(accountSummaryPayload, that.accountSummaryPayload);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId, enrolledAt, enrollmentType, accountSummaryPayload);
    }

    @Override
    public String toString() {
        return "CohortEnrollmentPayload{" +
                "cohortId=" + cohortId +
                ", enrolledAt='" + enrolledAt + '\'' +
                ", enrollmentType=" + enrollmentType +
                ", accountSummaryPayload=" + accountSummaryPayload +
                '}';
    }
}
