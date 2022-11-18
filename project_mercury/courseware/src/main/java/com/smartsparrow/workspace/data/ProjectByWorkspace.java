package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ProjectByWorkspace extends WorkspaceProject {

    private String name;
    private String createdAt;

    public String getName() {
        return name;
    }

    public ProjectByWorkspace setName(final String name) {
        this.name = name;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ProjectByWorkspace setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public ProjectByWorkspace setProjectId(final UUID projectId) {
        super.setProjectId(projectId);
        return this;
    }

    @Override
    public ProjectByWorkspace setWorkspaceId(final UUID workspaceId) {
        super.setWorkspaceId(workspaceId);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProjectByWorkspace that = (ProjectByWorkspace) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, createdAt);
    }

    @Override
    public String toString() {
        return "ProjectByWorkspace{" +
                "name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
