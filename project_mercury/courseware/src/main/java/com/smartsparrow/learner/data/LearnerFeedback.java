package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;

public class LearnerFeedback extends Feedback implements LearnerElement {

    private static final long serialVersionUID = -1803886115302257502L;

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerFeedback setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerFeedback setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.FEEDBACK;
    }

    @Override
    public String getConfig() {
        return config;
    }

    public LearnerFeedback setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerFeedback setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerFeedback setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnerFeedback setPluginVersionExpr(String pluginVersionExpr) {
        super.setPluginVersionExpr(pluginVersionExpr);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerFeedback that = (LearnerFeedback) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId, config);
    }

    @Override
    public String toString() {
        return "LearnerFeedback{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                "} " + super.toString();
    }
}
