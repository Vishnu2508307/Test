package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentPathwayByInteractiveMutator extends SimpleTableMutator<UUID> {

    public Statement deleteBy(UUID interactiveId) {
        String DELETE = "DELETE FROM courseware.parent_pathway_by_interactive " +
                "WHERE interactive_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement insert(UUID interactiveId, UUID pathwayId) {
        String INSERT = "INSERT INTO courseware.parent_pathway_by_interactive (" +
                "interactive_id, " +
                "pathway_id " +
                ") VALUES ( ?, ? )";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(interactiveId, pathwayId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
