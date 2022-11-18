package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class Project {

    private UUID id;
    private String name;
    private String config;
    private UUID workspaceId;
    private String createdAt;

    public UUID getId() {
        return id;
    }

    public Project setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Project setName(final String name) {
        this.name = name;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public Project setConfig(final String config) {
        this.config = config;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public Project setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Project setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(name, project.name) &&
                Objects.equals(config, project.config) &&
                Objects.equals(workspaceId, project.workspaceId) &&
                Objects.equals(createdAt, project.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, config, workspaceId, createdAt);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", config='" + config + '\'' +
                ", workspaceId=" + workspaceId +
                '}';
    }
}
