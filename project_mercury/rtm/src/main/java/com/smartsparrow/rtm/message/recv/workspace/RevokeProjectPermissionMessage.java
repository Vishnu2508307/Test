package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeProjectPermissionMessage extends ReceivedMessage implements ProjectMessage {

    private UUID accountId;
    private UUID teamId;
    private UUID projectId;

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeProjectPermissionMessage that = (RevokeProjectPermissionMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, teamId, projectId);
    }

    @Override
    public String toString() {
        return "RevokeProjectPermissionMessage{" +
                "accountId=" + accountId +
                ", teamId=" + teamId +
                ", projectId=" + projectId +
                "} " + super.toString();
    }
}
