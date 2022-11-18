package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

/**
 * This POJO is used for workspace log messages in Cassandra
 * */
public class WorkspaceLogStatement extends GenericLogStatement {

    private UUID projectId;
    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getProjectId() {
        return projectId;
    }

    public WorkspaceLogStatement setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public WorkspaceLogStatement setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public WorkspaceLogStatement setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @Override
    public WorkspaceLogStatement setPluginId(final UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public WorkspaceLogStatement setVersion(final String version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public WorkspaceLogStatement setBucketId(final UUID bucketId) {
        super.setBucketId(bucketId);
        return this;
    }

    @Override
    public WorkspaceLogStatement setLevel(final PluginLogLevel level) {
        super.setLevel(level);
        return this;
    }

    @Override
    public WorkspaceLogStatement setId(final UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public WorkspaceLogStatement setMessage(final String message) {
        super.setMessage(message);
        return this;
    }

    @Override
    public WorkspaceLogStatement setArgs(final String args) {
        super.setArgs(args);
        return this;
    }

    @Override
    public WorkspaceLogStatement setPluginContext(final String pluginContext) {
        super.setPluginContext(pluginContext);
        return this;
    }

    @Override
    public WorkspaceLogStatement setLoggingContext(final PluginLogContext loggingContext) {
        super.setLoggingContext(loggingContext);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorkspaceLogStatement that = (WorkspaceLogStatement) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), projectId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "WorkspaceLogStatement{" +
                "projectId=" + projectId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                "} " + super.toString();
    }
}
