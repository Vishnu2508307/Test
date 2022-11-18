package com.smartsparrow.rtm.message.recv.competency;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantDocumentPermissionMessage extends ReceivedMessage implements DocumentMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID documentId;
    private PermissionLevel permissionLevel;

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public GrantDocumentPermissionMessage setAccountIds(List<UUID> accountIds) {
        this.accountIds = accountIds;
        return this;
    }

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public GrantDocumentPermissionMessage setTeamIds(List<UUID> teamIds) {
        this.teamIds = teamIds;
        return this;
    }

    public GrantDocumentPermissionMessage setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public GrantDocumentPermissionMessage setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public UUID getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantDocumentPermissionMessage that = (GrantDocumentPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(documentId, that.documentId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, documentId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantDocumentPermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", documentId=" + documentId +
                ", permissionLevel=" + permissionLevel +
                "} " + super.toString();
    }
}
