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

public class ParentByLearnerComponentMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ParentByLearnerComponentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByLatestDeployment(UUID componentId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "component_id, " +
                "deployment_id, " +
                "change_id, " +
                "parent_id, " +
                "parent_type " +
                "FROM learner.parent_by_component " +
                "WHERE component_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ParentByLearnerComponent fromRow(Row row) {
        return new ParentByLearnerComponent()
                .setComponentId(row.getUUID("component_id"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"));
    }
}
