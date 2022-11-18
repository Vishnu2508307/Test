package com.smartsparrow.workspace.payload;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.data.Project;

public class ProjectPayload {

    private UUID id;
    private String name;
    private String config;
    private UUID workspaceId;
    private String createdAt;
    PermissionLevel permissionLevel;

    public static ProjectPayload from(@Nonnull Project project, @Nonnull PermissionLevel highestPermission) {
        ProjectPayload result = new ProjectPayload();
        result.id = project.getId();
        result.name = project.getName();
        result.config = project.getConfig();
        result.workspaceId = project.getWorkspaceId();
        result.createdAt = project.getCreatedAt();
        result.permissionLevel = highestPermission;

        return result;
    }

    public UUID getId() {
        return id;
    }

    public ProjectPayload setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProjectPayload setName(final String name) {
        this.name = name;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ProjectPayload setConfig(final String config) {
        this.config = config;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ProjectPayload setWorkspaceId(final UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ProjectPayload setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public ProjectPayload setPermissionLevel(final PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectPayload project = (ProjectPayload) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(name, project.name) &&
                Objects.equals(config, project.config) &&
                Objects.equals(workspaceId, project.workspaceId) &&
                Objects.equals(createdAt, project.createdAt) &&
                Objects.equals(permissionLevel, project.permissionLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, config, workspaceId, createdAt, permissionLevel);
    }

    @Override
    public String toString() {
        return "ProjectPayload{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", config='" + config + '\'' +
                ", workspaceId=" + workspaceId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
