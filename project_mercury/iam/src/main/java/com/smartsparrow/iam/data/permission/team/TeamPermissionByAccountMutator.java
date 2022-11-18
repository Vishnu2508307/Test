package com.smartsparrow.iam.data.permission.team;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamPermissionByAccountMutator extends SimpleTableMutator<TeamPermission> {

    @Override
    public String getUpsertQuery(TeamPermission mutation) {
        return "INSERT INTO iam_global.team_permission_by_account (" +
                "account_id, " +
                "team_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(TeamPermission mutation) {
        return "DELETE FROM iam_global.team_permission_by_account " +
                "WHERE account_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getTeamId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getTeamId());
    }
}
