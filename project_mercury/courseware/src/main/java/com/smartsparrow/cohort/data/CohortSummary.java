package com.smartsparrow.cohort.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CohortSummary implements Serializable {

    private static final long serialVersionUID = 4788733780202299055L;
    private UUID id;
    private String name;
    private EnrollmentType type;
    private Long startDate;
    private Long endDate;
    private UUID finishedDate;
    private UUID workspaceId;
    private UUID creatorId;
    private UUID subscriptionId;

    public UUID getId() {
        return id;
    }

    public CohortSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CohortSummary setName(String name) {
        this.name = name;
        return this;
    }

    public EnrollmentType getType() {
        return type;
    }

    public CohortSummary setType(EnrollmentType type) {
        this.type = type;
        return this;
    }

    public Long getStartDate() {
        return startDate;
    }

    public CohortSummary setStartDate(Long startDate) {
        this.startDate = startDate;
        return this;
    }

    public Long getEndDate() {
        return endDate;
    }

    public CohortSummary setEndDate(Long endDate) {
        this.endDate = endDate;
        return this;
    }

    public UUID getFinishedDate() {
        return finishedDate;
    }

    public CohortSummary setFinishedDate(UUID finishedDate) {
        this.finishedDate = finishedDate;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public CohortSummary setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public CohortSummary setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public CohortSummary setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    @JsonIgnore
    public boolean isFinished() {
        return this.finishedDate != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CohortSummary that = (CohortSummary) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type && Objects.equals(
                startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(finishedDate,
                                                                                                      that.finishedDate)
                && Objects.equals(workspaceId, that.workspaceId) && Objects.equals(creatorId, that.creatorId) && Objects
                .equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, startDate, endDate, finishedDate, workspaceId, creatorId, subscriptionId);
    }

    @Override
    public String toString() {
        return "CohortSummary{" + "id=" + id + ", name='" + name + '\'' + ", type=" + type + ", startDate=" + startDate
                + ", endDate=" + endDate + ", finishedDate=" + finishedDate + ", workspaceId=" + workspaceId
                + ", creatorId=" + creatorId + ", subscriptionId=" + subscriptionId + '}';
    }
}
