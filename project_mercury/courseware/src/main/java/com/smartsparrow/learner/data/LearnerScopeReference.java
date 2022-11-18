package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScopeReference;

public class LearnerScopeReference extends ScopeReference {

    private UUID deploymentId;
    private UUID changeId;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerScopeReference setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerScopeReference setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public LearnerScopeReference setElementId(UUID elementId) {
        super.setElementId(elementId);
        return this;
    }

    @Override
    public LearnerScopeReference setScopeURN(UUID scopeURN) {
        super.setScopeURN(scopeURN);
        return this;
    }

    @Override
    public LearnerScopeReference setElementType(CoursewareElementType elementType) {
        super.setElementType(elementType);
        return this;
    }

    @Override
    public LearnerScopeReference setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnerScopeReference setPluginVersion(String pluginVersion) {
        super.setPluginVersion(pluginVersion);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerScopeReference that = (LearnerScopeReference) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId);
    }

    @Override
    public String toString() {
        return "LearnerScopeReference{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                "} " + super.toString();
    }
}
