package com.smartsparrow.iam.data.permission.workspace;

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

public class AccountProjectPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountProjectPermissionMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermissionLevel(final UUID accountId, final UUID projectId) {
        final String QUERY = "SELECT " +
                "permission_level " +
                "FROM iam_global.project_permission_by_account " +
                "WHERE account_id = ?" +
                "AND project_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, projectId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public PermissionLevel fromRow(Row row) {
        return Enums.of(PermissionLevel.class, row.getString("permission_level"));
    }
}
