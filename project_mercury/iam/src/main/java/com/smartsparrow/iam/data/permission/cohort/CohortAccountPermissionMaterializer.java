package com.smartsparrow.iam.data.permission.cohort;

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

public class CohortAccountPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortAccountPermissionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID accountId, UUID cohortId) {
        final String BY_COHORT = "SELECT account_id, " +
                "cohort_id, " +
                "permission_level " +
                "FROM iam_global.cohort_permission_by_account " +
                "WHERE account_id = ? " +
                "AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchPermission(UUID accountId) {
        final String BY_ACCOUNT = "SELECT account_id, " +
                "cohort_id, " +
                "permission_level " +
                "FROM iam_global.cohort_permission_by_account " +
                "WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Conver a cassandra row to an account cohort permission
     *
     * @param row the {@link Row} to convert
     * @return a {@link AccountCohortPermission} object
     */
    public AccountCohortPermission fromRow(Row row) {
        return new AccountCohortPermission()
                .setAccountId(row.getUUID("account_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
