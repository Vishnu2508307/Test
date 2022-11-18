package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class SubscriptionByTeamMutator extends SimpleTableMutator<SubscriptionTeam> {

    @Override
    public String getUpsertQuery(SubscriptionTeam mutation) {
        return "INSERT INTO subscription.subscription_by_team (" +
                "team_id, " +
                "subscription_id) " +
                "VALUES (?,?)";    }

    @Override
    public String getDeleteQuery(SubscriptionTeam mutation) {
        return "DELETE FROM subscription.subscription_by_team " +
                "WHERE team_id = ? " +
                "AND subscription_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SubscriptionTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getSubscriptionId());
    }

    @Override
    public void bindDelete(BoundStatement stmt, SubscriptionTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getSubscriptionId());
    }
}
