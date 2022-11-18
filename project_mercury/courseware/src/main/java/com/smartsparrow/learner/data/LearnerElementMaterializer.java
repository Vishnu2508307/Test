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

public class LearnerElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByDeployment(UUID id, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "element_type " +
                "FROM learner.element " +
                "WHERE id = ?" +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerCoursewareElement fromRow(Row row) {
        return new LearnerCoursewareElement()
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
