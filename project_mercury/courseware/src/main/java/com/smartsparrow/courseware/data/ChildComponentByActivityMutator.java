package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildComponentByActivityMutator extends SimpleTableMutator<UUID> {

    public Statement insert(UUID componentId, UUID activityId) {
        final String INSERT = "INSERT INTO courseware.child_component_by_activity (" +
                "component_id, " +
                "activity_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId, activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement deleteComponent(UUID componentId, UUID activityId) {
        String DELETE = "DELETE FROM courseware.child_component_by_activity " +
                "WHERE component_id = ?" +
                "AND activity_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId, activityId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
