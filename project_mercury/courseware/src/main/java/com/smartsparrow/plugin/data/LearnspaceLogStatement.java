package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

/**
 * This POJO is used for learnspace log messages in Cassandra
 * */
public class LearnspaceLogStatement extends GenericLogStatement {

    private UUID elementId;
    private CoursewareElementType elementType;
    private UUID deploymentId;
    private UUID cohortId;

    public UUID getElementId() {
        return elementId;
    }

    public LearnspaceLogStatement setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public LearnspaceLogStatement setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnspaceLogStatement setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public LearnspaceLogStatement setCohortId(final UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    @Override
    public LearnspaceLogStatement setPluginId(final UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnspaceLogStatement setVersion(final String version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public LearnspaceLogStatement setBucketId(final UUID bucketId) {
        super.setBucketId(bucketId);
        return this;
    }

    @Override
    public LearnspaceLogStatement setLevel(final PluginLogLevel level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public LearnspaceLogStatement setId(final UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnspaceLogStatement setMessage(final String message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public LearnspaceLogStatement setArgs(final String args) {
        super.setArgs(args);
        return this;
    }

    @Override
    public LearnspaceLogStatement setPluginContext(final String pluginContext) {
        super.setPluginContext(pluginContext);
        return this;
    }

    @Override
    public LearnspaceLogStatement setLoggingContext(final PluginLogContext loggingContext) {
        super.setLoggingContext(loggingContext);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnspaceLogStatement that = (LearnspaceLogStatement) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elementId, elementType, deploymentId, cohortId);
    }

    @Override
    public String toString() {
        return "LearnspaceLogStatement{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", deploymentId=" + deploymentId +
                ", cohortId=" + cohortId +
                "} " + super.toString();
    }
}
