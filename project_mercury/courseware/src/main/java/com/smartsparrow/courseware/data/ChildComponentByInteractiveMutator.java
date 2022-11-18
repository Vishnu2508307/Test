package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ChildComponentByInteractiveMutator extends SimpleTableMutator<UUID> {

    public Statement insert(UUID componentId, UUID interactiveId) {
        // @formatter:off
        String QUERY = "INSERT INTO courseware.child_component_by_interactive ("
                        + "  component_id"
                        + ", interactive_id"
                        + ") VALUES ( ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId, interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement deleteComponent(UUID componentId, UUID interactiveId) {
        String DELETE = "DELETE FROM courseware.child_component_by_interactive " +
                "WHERE component_id = ? " +
                "AND interactive_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId, interactiveId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
