package com.smartsparrow.iam.data.permission.subscription;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamSubscriptionPermissionMutator extends SimpleTableMutator<TeamSubscriptionPermission> {

    @Override
    public String getUpsertQuery(TeamSubscriptionPermission mutation) {
        return "INSERT INTO iam_global.subscription_permission_by_team ("
                + "  team_id"
                + ", subscription_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamSubscriptionPermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getSubscriptionId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamSubscriptionPermission mutation) {
        return "DELETE FROM iam_global.subscription_permission_by_team " +
                "WHERE team_id = ? " +
                "AND subscription_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamSubscriptionPermission mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getSubscriptionId());
    }
}
