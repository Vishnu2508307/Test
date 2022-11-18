package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.collaborator.TeamCollaborator;
import com.smartsparrow.iam.service.PermissionLevel;

public class TeamDocumentCollaborator extends DocumentCollaborator implements TeamCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamDocumentCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public TeamDocumentCollaborator setDocumentId(UUID documentId) {
        super.setDocumentId(documentId);
        return this;
    }

    @Override
    public TeamDocumentCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TeamDocumentCollaborator that = (TeamDocumentCollaborator) o;
        return Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamId);
    }

    @Override
    public String toString() {
        return "TeamDocumentCollaborator{" +
                "teamId=" + teamId +
                "} " + super.toString();
    }
}
