package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;

public class LearnerElementMetaInformation extends CoursewareElementMetaInformation {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerElementMetaInformation setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerElementMetaInformation setChangeId(final UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerElementMetaInformation setKey(final String key) {
        super.setKey(key);
        return this;
    }

    @Override
    public LearnerElementMetaInformation setValue(final String value) {
        super.setValue(value);
        return this;
    }

    @Override
    public LearnerElementMetaInformation setElementId(final UUID elementId) {
        super.setElementId(elementId);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerElementMetaInformation that = (LearnerElementMetaInformation) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerElementMetaInformation{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
