package com.smartsparrow.iam.data.team;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountByTeamMutator extends SimpleTableMutator<AccountTeamCollaborator> {

    @Override
    public String getUpsertQuery(AccountTeamCollaborator mutation) {
        // @formatter:off
        return "INSERT INTO team.account_by_team ("
                + "  team_id"
                + ", account_id"
                + ", permission_level"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountTeamCollaborator mutation) {
        stmt.bind(mutation.getTeamId(),
                mutation.getAccountId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(AccountTeamCollaborator mutation) {
        return "DELETE FROM team.account_by_team " +
                "WHERE team_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountTeamCollaborator mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getAccountId());
    }

}
