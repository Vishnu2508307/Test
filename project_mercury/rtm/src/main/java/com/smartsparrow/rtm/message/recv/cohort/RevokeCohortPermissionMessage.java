package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RevokeCohortPermissionMessage extends ReceivedMessage implements CohortMessage {

    private UUID accountId;
    private UUID teamId;
    private UUID cohortId;

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeCohortPermissionMessage that = (RevokeCohortPermissionMessage) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(teamId, that.teamId) &&
                Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, teamId, cohortId);
    }

    @Override
    public String toString() {
        return "RevokeCohortPermissionMessage{" +
                "accountId=" + accountId +
                ", teamId=" + teamId +
                ", cohortId=" + cohortId +
                "} " + super.toString();
    }
}
