package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LearnerComponent extends Component implements LearnerElement {

    private static final long serialVersionUID = -8861121856344520127L;

    private UUID deploymentId;
    private UUID changeId;
    private String config;

    @GraphQLIgnore
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerComponent setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    public UUID getChangeId() {
        return changeId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.COMPONENT;
    }

    public LearnerComponent setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public String getConfig() {
        return config;
    }

    public LearnerComponent setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerComponent setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerComponent setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnerComponent setPluginVersionExpr(String pluginVersionExpr) {
        super.setPluginVersionExpr(pluginVersionExpr);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerComponent that = (LearnerComponent) o;
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
        return "LearnerComponent{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                "} " + super.toString();
    }
}
