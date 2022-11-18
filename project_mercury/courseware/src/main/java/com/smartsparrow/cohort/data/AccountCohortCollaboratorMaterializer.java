package com.smartsparrow.cohort.data;

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

public class AccountCohortCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountCohortCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAccounts(UUID cohortId) {
        final String BY_COHORT = "SELECT cohort_id, " +
                "account_id, " +
                "permission_level " +
                "FROM cohort.account_by_cohort " +
                "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;

    }

    public Statement fetchAccount(UUID cohortId, UUID accountId) {
        final String BY_ACCOUNT = "SELECT cohort_id, " +
                "account_id, " +
                "permission_level " +
                "FROM cohort.account_by_cohort " +
                "WHERE cohort_id = ? " +
                "AND account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to a cohort collaborator object
     *
     * @param row the {@link Row} to convert
     * @return a {@link AccountCohortCollaborator}
     */
    public AccountCohortCollaborator fromRow(Row row) {
        return new AccountCohortCollaborator()
                .setAccountId(row.getUUID("account_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
