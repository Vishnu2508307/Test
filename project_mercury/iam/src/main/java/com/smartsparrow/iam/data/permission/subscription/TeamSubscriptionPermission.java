package com.smartsparrow.iam.data.permission.subscription;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class TeamSubscriptionPermission {

    private UUID teamId;
    private UUID subscriptionId;
    private PermissionLevel permissionLevel;

    public UUID getTeamId() {
        return teamId;
    }

    public TeamSubscriptionPermission setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public TeamSubscriptionPermission setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public TeamSubscriptionPermission setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamSubscriptionPermission that = (TeamSubscriptionPermission) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, subscriptionId, permissionLevel);
    }

    @Override
    public String toString() {
        return "TeamSubscriptionPermission{" +
                "teamId=" + teamId +
                ", subscriptionId=" + subscriptionId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
