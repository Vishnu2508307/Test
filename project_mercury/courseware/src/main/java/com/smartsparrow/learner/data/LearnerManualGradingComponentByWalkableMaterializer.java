package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerManualGradingComponentByWalkableMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerManualGradingComponentByWalkableMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAll(UUID deploymentId, UUID walkableId) {
        final String SELECT = "SELECT" +
                " deployment_id" +
                ", walkable_id" +
                ", component_id" +
                ", change_id" +
                ", walkable_type" +
                ", component_parent_id" +
                ", component_parent_type" +
                " FROM learner.manual_grading_component_by_walkable" +
                " WHERE deployment_id = ?" +
                " AND walkable_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, walkableId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerManualGradingComponentByWalkable fromRow(Row row) {
        return new LearnerManualGradingComponentByWalkable()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setWalkableId(row.getUUID("walkable_id"))
                .setChangeId(row.getUUID("change_id"))
                .setComponentId(row.getUUID("component_id"))
                .setWalkableType(Enums.of(CoursewareElementType.class, row.getString("walkable_type")))
                .setComponentParentId(row.getUUID("component_parent_id"))
                .setComponentParentType(Enums.of(CoursewareElementType.class, row.getString("component_parent_type")));
    }
}
