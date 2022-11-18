package com.smartsparrow.cohort.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortSummaryMutator extends SimpleTableMutator<CohortSummary> {

    @Override
    public String getUpsertQuery(CohortSummary mutation) {
        // @formatter:off
        return "INSERT INTO cohort.summary ("
                + "  id"
                + ", name"
                + ", type"
                + ", start_date"
                + ", end_date"
                + ", finished_date"
                + ", workspace_id"
                + ", creator_id"
                + ", subscription_id"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CohortSummary mutation) {
        stmt.bind(mutation.getId(),
                mutation.getName(),
                mutation.getType().name(),
                mutation.getStartDate(),
                mutation.getEndDate(),
                mutation.getFinishedDate(),
                mutation.getWorkspaceId(),
                mutation.getCreatorId(),
                mutation.getSubscriptionId());

    }

    public Statement update(CohortSummary mutation) {
        String SET_NAME = "UPDATE cohort.summary " +
                "SET name = ?, " +
                "    type = ?, " +
                "    start_date = ?, " +
                "    end_date = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_NAME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(mutation.getName(), mutation.getType().name(), mutation.getStartDate(), mutation.getEndDate(), mutation.getId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setName(UUID id, String name) {
        String SET_NAME = "UPDATE cohort.summary " +
                "SET name = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_NAME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(name, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setType(UUID id, EnrollmentType enrollmentType) {
        String SET_TYPE = "UPDATE cohort.summary " +
                "SET type = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_TYPE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(enrollmentType.name(), id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setWorkspace(UUID id, UUID workspaceId) {
        String SET_WORKSPACE = "UPDATE cohort.summary " +
                "SET workspace_id = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_WORKSPACE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setStartDate(UUID id, Long startDate) {
        String SET_START_DATE = "UPDATE cohort.summary " +
                "SET start_date = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_START_DATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(startDate, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setEndDate(UUID id, Long endDate) {
        String SET_END_DATE = "UPDATE cohort.summary " +
                "SET end_date = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_END_DATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(endDate, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    Statement setFinished(UUID id, UUID finishedDate) {
        String SET_FINISHED = "UPDATE cohort.summary " +
                "SET finished_date = ? " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SET_FINISHED);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(finishedDate, id);
        stmt.setIdempotent(true);
        return stmt;
    }
}
