package com.smartsparrow.iam.data.team;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamBySubscriptionMutator extends SimpleTableMutator<TeamBySubscription> {

    @Override
    public String getUpsertQuery(TeamBySubscription mutation) {
        // @formatter:off
        return "INSERT INTO team.team_by_subscription ("
                + "  subscription_id"
                + ", team_id"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamBySubscription mutation) {
        stmt.bind(mutation.getSubscriptionId(),
                mutation.getTeamId()
        );
    }

    @Override
    public String getDeleteQuery(TeamBySubscription mutation) {
        return "DELETE FROM team.team_by_subscription " +
                "WHERE subscription_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamBySubscription mutation) {
        stmt.bind(mutation.getSubscriptionId(), mutation.getTeamId());
    }
}
