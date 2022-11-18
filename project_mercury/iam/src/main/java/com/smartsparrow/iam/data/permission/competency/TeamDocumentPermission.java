package com.smartsparrow.iam.data.permission.competency;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamDocumentPermission {

    private UUID teamId;
    private UUID documentId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamDocumentPermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public TeamDocumentPermission setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamDocumentPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDocumentPermission that = (TeamDocumentPermission) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(documentId, that.documentId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, documentId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamDocumentPermission{" +
                "teamId=" + teamId +
                ", documentId=" + documentId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
