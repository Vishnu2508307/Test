package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CohortByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchCohorts(UUID accountId) {
        final String BY_ACCOUNT = "SELECT account_id, " +
                "cohort_id " +
                "FROM cohort.cohort_by_account " +
                "WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to a cohort account object
     *
     * @param row the {@link Row} to convert
     * @return a {@link CohortAccount}
     */
    public CohortAccount fromRow(Row row) {
        return new CohortAccount()
                .setAccountId(row.getUUID("account_id"))
                .setCohortId(row.getUUID("cohort_id"));
    }
}
