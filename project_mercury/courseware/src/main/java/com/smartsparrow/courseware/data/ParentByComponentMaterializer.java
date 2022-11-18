package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ParentByComponentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ParentByComponentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchById(UUID componentId) {
        final String SELECT = "SELECT " +
                "component_id, " +
                "parent_id, " +
                "parent_type " +
                "FROM courseware.parent_by_component " +
                "WHERE component_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ParentByComponent fromRow(Row row) {
        return new ParentByComponent()
                .setComponentId(row.getUUID("component_id"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enum.valueOf(CoursewareElementType.class, row.getString("parent_type")));
    }
}
