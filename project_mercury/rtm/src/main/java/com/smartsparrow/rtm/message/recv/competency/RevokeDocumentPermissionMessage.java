package com.smartsparrow.rtm.message.recv.competency;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeDocumentPermissionMessage extends ReceivedMessage implements DocumentMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID documentId;

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public RevokeDocumentPermissionMessage setAccountIds(List<UUID> accountIds) {
        this.accountIds = accountIds;
        return this;
    }

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public RevokeDocumentPermissionMessage setTeamIds(List<UUID> teamIds) {
        this.teamIds = teamIds;
        return this;
    }

    public RevokeDocumentPermissionMessage setDocumentId(UUID documentId) {
        this.documentId = documentId;
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
        RevokeDocumentPermissionMessage that = (RevokeDocumentPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, documentId);
    }

    @Override
    public String toString() {
        return "RevokeDocumentPermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", documentId=" + documentId +
                "} " + super.toString();
    }
}
