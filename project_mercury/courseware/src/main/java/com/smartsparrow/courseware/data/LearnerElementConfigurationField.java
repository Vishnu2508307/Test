package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class LearnerElementConfigurationField extends CoursewareElementConfigurationField {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerElementConfigurationField setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerElementConfigurationField setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerElementConfigurationField setElementId(UUID elementId) {
        super.setElementId(elementId);
        return this;
    }

    @Override
    public LearnerElementConfigurationField setFieldName(String fieldName) {
        super.setFieldName(fieldName);
        return this;
    }

    @Override
    public LearnerElementConfigurationField setFieldValue(String fieldValue) {
        super.setFieldValue(fieldValue);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerElementConfigurationField that = (LearnerElementConfigurationField) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerElementConfigurationField{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "}" + super.toString();
    }
}
