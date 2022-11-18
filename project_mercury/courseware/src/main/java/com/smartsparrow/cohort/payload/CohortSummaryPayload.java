package com.smartsparrow.cohort.payload;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.util.DateFormat;

public class CohortSummaryPayload {

    private UUID cohortId;
    private UUID workspaceId;
    private String name;
    private EnrollmentType enrollmentType;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String finishedDate;
    private long enrollmentsCount;
    private AccountPayload creator;

    public UUID getCohortId() {
        return cohortId;
    }

    public CohortSummaryPayload setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public CohortSummaryPayload setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getName() {
        return name;
    }

    public CohortSummaryPayload setName(String name) {
        this.name = name;
        return this;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public CohortSummaryPayload setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public CohortSummaryPayload setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public CohortSummaryPayload setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public CohortSummaryPayload setCreatedAt(String creationDate) {
        this.createdAt = creationDate;
        return this;
    }

    public String getFinishedDate() {
        return finishedDate;
    }

    public CohortSummaryPayload setFinishedDate(String finishedDate) {
        this.finishedDate = finishedDate;
        return this;
    }

    public long getEnrollmentsCount() {
        return enrollmentsCount;
    }

    public CohortSummaryPayload setEnrollmentsCount(long enrollmentsCount) {
        this.enrollmentsCount = enrollmentsCount;
        return this;
    }

    public AccountPayload getCreator() {
        return creator;
    }

    public CohortSummaryPayload setCreator(AccountPayload creator) {
        this.creator = creator;
        return this;
    }

    public static CohortSummaryPayload from(@Nonnull CohortSummary cohortSummary,
                                            long enrollmentsCount,
                                            AccountPayload accountPayload) {
        return new CohortSummaryPayload()
                .setName(cohortSummary.getName())
                .setCreatedAt(DateFormat.asRFC1123(cohortSummary.getId()))
                .setEnrollmentType(cohortSummary.getType())
                .setStartDate(cohortSummary.getStartDate() != null ?
                        DateFormat.asRFC1123(cohortSummary.getStartDate()) : null)
                .setEndDate(cohortSummary.getEndDate() != null ?
                        DateFormat.asRFC1123(cohortSummary.getEndDate()) : null)
                .setFinishedDate(cohortSummary.getFinishedDate() != null ?
                        DateFormat.asRFC1123(cohortSummary.getFinishedDate()): null)
                .setEnrollmentsCount(enrollmentsCount)
                .setCreator(accountPayload)
                .setWorkspaceId(cohortSummary.getWorkspaceId())
                .setCohortId(cohortSummary.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSummaryPayload that = (CohortSummaryPayload) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(name, that.name) &&
                enrollmentType == that.enrollmentType &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(finishedDate, that.finishedDate) &&
                Objects.equals(enrollmentsCount, that.enrollmentsCount) &&
                Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId, workspaceId, name, enrollmentType, startDate, endDate, createdAt, finishedDate,
                enrollmentsCount, creator);
    }

    @Override
    public String toString() {
        return "CohortSummaryPayload{" +
                "cohortId=" + cohortId +
                ", workspaceId=" + workspaceId +
                ", name='" + name + '\'' +
                ", enrollmentType=" + enrollmentType +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", finishedDate='" + finishedDate + '\'' +
                ", enrollmentsCount=" + enrollmentsCount +
                ", creator=" + creator +
                '}';
    }
}
