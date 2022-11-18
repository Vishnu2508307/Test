package com.smartsparrow.iam.data;

import java.util.UUID;

import com.smartsparrow.iam.service.PermissionLevel;

public class SubscriptionTeamCollaborator extends SubscriptionCollaborator {

    private UUID teamId;

    public UUID getTeamId() {
        return teamId;
    }

    public SubscriptionTeamCollaborator setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public SubscriptionTeamCollaborator setSubscriptionId(UUID subscriptionId) {
        super.setSubscriptionId(subscriptionId);
        return this;
    }

    @Override
    public SubscriptionTeamCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        super.setPermissionLevel(permissionLevel);
        return this;
    }
}
