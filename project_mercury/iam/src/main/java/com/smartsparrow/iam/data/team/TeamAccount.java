package com.smartsparrow.iam.data.team;

import java.util.Objects;
import java.util.UUID;

public class TeamAccount {
    private UUID teamId;
    private UUID accountId;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamAccount setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TeamAccount setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamAccount that = (TeamAccount) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(teamId, accountId);
    }

    @Override
    public String toString() {
        return "TeamAccount{" +
                "teamId=" + teamId +
                ", accountId=" + accountId +
                '}';
    }
}
