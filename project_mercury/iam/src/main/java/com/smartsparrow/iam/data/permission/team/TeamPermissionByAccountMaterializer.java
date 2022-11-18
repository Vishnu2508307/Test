package com.smartsparrow.iam.data.permission.team;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class TeamPermissionByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public TeamPermissionByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID accountId, UUID teamId) {
        final String BY_ACCOUNT_WORKSPACE = "SELECT account_id, " +
                "team_id, " +
                "permission_level " +
                "FROM iam_global.team_permission_by_account " +
                "WHERE account_id = ? " +
                "AND team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT_WORKSPACE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert cassandra row to TeamPermission type
     *
     * @param row {@link Row}
     * @return {@link TeamPermission}
     */
    protected TeamPermission fromRow(Row row) {
        return new TeamPermission()
                .setAccountId(row.getUUID("account_id"))
                .setTeamId(row.getUUID("team_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
