package com.smartsparrow.iam.data.team;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamByAccountMutator extends SimpleTableMutator<TeamAccount> {

    @Override
    public String getUpsertQuery(TeamAccount mutation) {
        // @formatter:off
        return "INSERT INTO team.team_by_account ("
                + "  account_id"
                + ", team_id"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamAccount mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getTeamId()
        );
    }

    @Override
    public String getDeleteQuery(TeamAccount mutation) {
        return "DELETE FROM team.team_by_account " +
                "WHERE account_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getTeamId());
    }

}
