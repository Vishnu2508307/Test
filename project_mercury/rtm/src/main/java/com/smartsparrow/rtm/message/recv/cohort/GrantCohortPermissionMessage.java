package com.smartsparrow.rtm.message.recv.cohort;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GrantCohortPermissionMessage extends ReceivedMessage implements CohortMessage {

    private List<UUID> accountIds;
    private List<UUID> teamIds;
    private UUID cohortId;
    private PermissionLevel permissionLevel;

    public List<UUID> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(List<UUID> teamIds) {
        this.teamIds = teamIds;
    }

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public List<UUID> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<UUID> accountIds) {
        this.accountIds = accountIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantCohortPermissionMessage that = (GrantCohortPermissionMessage) o;
        return Objects.equals(accountIds, that.accountIds) &&
                Objects.equals(teamIds, that.teamIds) &&
                Objects.equals(cohortId, that.cohortId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountIds, teamIds, cohortId, permissionLevel);
    }

    @Override
    public String toString() {
        return "GrantCohortPermissionMessage{" +
                "accountIds=" + accountIds +
                ", teamIds=" + teamIds +
                ", cohortId=" + cohortId +
                ", permissionLevel=" + permissionLevel +
                "} " + super.toString();
    }
}
