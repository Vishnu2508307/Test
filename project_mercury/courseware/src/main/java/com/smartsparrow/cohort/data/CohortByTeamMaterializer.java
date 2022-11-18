package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CohortByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortByTeamMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findCohorts(UUID teamId) {
        final String BY_GROUP = "SELECT cohort_id " +
                "FROM cohort.cohort_by_team " +
                "WHERE team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_GROUP);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("cohort_id");
    }
}
