package com.smartsparrow.cohort.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortByTeamMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(UUID cohortId, UUID teamId) {
        String upsertQuery = "INSERT INTO cohort.cohort_by_team (" +
                "team_id, " +
                "cohort_id) VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(upsertQuery);
        stmt.setConsistencyLevel(upsertConsistencyLevel());
        stmt.setIdempotent(isUpsertIdempotent());
        stmt.bind(teamId, cohortId);

        return stmt;
    }

    public Statement delete(UUID cohortId, UUID teamId) {
        String deleteQuery = "DELETE FROM cohort.cohort_by_team " +
                "WHERE team_id = ? " +
                "AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(deleteQuery);
        stmt.setConsistencyLevel(deleteConsistencyLevel());
        stmt.setIdempotent(isDeleteIdempotent());
        stmt.bind(teamId, cohortId);
        return stmt;
    }
}
