package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LearnerChildPathway {

    private UUID activityId;
    private UUID deploymentId;
    private UUID changeId;
    private List<UUID> pathwayIds;

    public UUID getActivityId() {
        return activityId;
    }

    public LearnerChildPathway setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerChildPathway setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerChildPathway setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public List<UUID> getPathwayIds() {
        return pathwayIds;
    }

    public LearnerChildPathway setPathwayIds(List<UUID> pathwayIds) {
        this.pathwayIds = pathwayIds;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerChildPathway that = (LearnerChildPathway) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(pathwayIds, that.pathwayIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, deploymentId, changeId, pathwayIds);
    }

    @Override
    public String toString() {
        return "LearnerChildPathway{" +
                "activityId=" + activityId +
                ", deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", pathwayIds=" + pathwayIds +
                '}';
    }
}
