package com.smartsparrow.iam.data.team;

import java.util.Objects;
import java.util.UUID;

public class TeamBySubscription {
    private UUID teamId;
    private UUID subscriptionId;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamBySubscription setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public TeamBySubscription setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamBySubscription that = (TeamBySubscription) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(teamId, subscriptionId);
    }

    @Override
    public String toString() {
        return "TeamBySubscription{" +
                "teamId=" + teamId +
                ", subscriptionId=" + subscriptionId +
                '}';
    }
}
