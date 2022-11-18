package com.smartsparrow.iam.data.permission.subscription;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;

public class TeamSubscriptionCollaboratorMutator extends SimpleTableMutator<SubscriptionTeamCollaborator> {

    @Override
    public String getUpsertQuery(SubscriptionTeamCollaborator mutation) {
        return "INSERT INTO subscription.team_by_subscription (" +
                "subscription_id, " +
                "team_id, " +
                "permission_level) " +
                "VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(SubscriptionTeamCollaborator mutation) {
        return "DELETE FROM subscription.team_by_subscription " +
                "WHERE subscription_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SubscriptionTeamCollaborator mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, SubscriptionTeamCollaborator mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getTeamId());
    }
}
