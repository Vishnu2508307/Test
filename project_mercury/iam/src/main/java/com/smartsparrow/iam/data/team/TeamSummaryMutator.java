package com.smartsparrow.iam.data.team;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamSummaryMutator extends SimpleTableMutator<TeamSummary> {

    @Override
    public String getUpsertQuery(TeamSummary mutation) {
        // @formatter:off
        return "INSERT INTO team.team_summary ("
                + "  id"
                + ", subscription_id"
                + ", name"
                + ", description"
                + ", thumbnail"
                + ") VALUES ( ?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamSummary mutation) {
        stmt.setUUID(0, mutation.getId());
        bindNonNull(stmt, 1, mutation.getSubscriptionId(), UUID.class);
        bindNonNull(stmt, 2, mutation.getName(), String.class);
        bindNonNull(stmt, 3, mutation.getDescription(), String.class);
        bindNonNull(stmt, 4, mutation.getThumbnail(), String.class);
    }

    @Override
    public String getDeleteQuery(TeamSummary mutation) {
        return "DELETE FROM team.team_summary " +
                "WHERE id = ? ";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamSummary mutation) {
        stmt.bind(mutation.getId());
    }

}
