package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.WalkablePathwayChildren;

public class LearnerWalkablePathwayChildren extends WalkablePathwayChildren {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerWalkablePathwayChildren setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerWalkablePathwayChildren setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerWalkablePathwayChildren setPathwayId(UUID pathwayId) {
        super.setPathwayId(pathwayId);
        return this;
    }

    @Override
    public LearnerWalkablePathwayChildren setWalkableIds(List<UUID> walkableIds) {
        super.setWalkableIds(walkableIds);
        return this;
    }

    @Override
    public LearnerWalkablePathwayChildren setWalkableTypes(Map<UUID, String> walkableTypes) {
        super.setWalkableTypes(walkableTypes);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerWalkablePathwayChildren that = (LearnerWalkablePathwayChildren) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerWalkablePathwayChildren{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
