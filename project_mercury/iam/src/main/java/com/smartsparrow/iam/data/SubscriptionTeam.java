package com.smartsparrow.iam.data;

import java.util.Objects;
import java.util.UUID;

/**
 * This class is intended to represent the relationship between a team and a subscription. Beware there are two kinds
 * of relationship:
 * <br> - <b>Access</b>: as in a team has access over a subscription
 * <br> - <b>Creation</b>: as in the team has been created in the subscription and therefore it belongs to the subscription
 * Beware of the context when finding this class to understand what relationship it describes
 */
public class SubscriptionTeam {

    private UUID subscriptionId;
    private UUID teamId;

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionTeam setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public SubscriptionTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionTeam that = (SubscriptionTeam) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, teamId);
    }

    @Override
    public String toString() {
        return "SubscriptionTeam{" +
                "subscriptionId=" + subscriptionId +
                ", teamId=" + teamId +
                '}';
    }
}
