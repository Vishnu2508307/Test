package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ManualGradingComponentByWalkableMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ManualGradingComponentByWalkableMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll(UUID walkableId) {
        final String SELECT = "SELECT" +
                " walkable_id" +
                ", component_id" +
                ", walkable_type" +
                ", component_parent_id" +
                ", component_parent_type" +
                " FROM courseware.manual_grading_component_by_walkable" +
                " WHERE walkable_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(walkableId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ManualGradingComponentByWalkable fromRow(Row row) {
        return new ManualGradingComponentByWalkable()
                .setWalkableId(row.getUUID("walkable_id"))
                .setComponentId(row.getUUID("component_id"))
                .setWalkableType(Enums.of(CoursewareElementType.class, row.getString("walkable_type")))
                .setComponentParentId(row.getUUID("component_parent_id"))
                .setParentComponentType(Enums.of(CoursewareElementType.class, row.getString("component_parent_type")));
    }
}
