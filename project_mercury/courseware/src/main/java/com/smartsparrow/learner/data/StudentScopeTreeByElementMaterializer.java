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

public class StudentScopeTreeByElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public StudentScopeTreeByElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findSubTree(UUID deploymentId, UUID studentId, UUID rootId) {
        final String SELECT = "SELECT" +
                " deployment_id" +
                ", student_id" +
                ", root_id" +
                ", scope_urn" +
                ", scope_id" +
                ", element_id" +
                ", element_type" +
                " FROM learner.student_scope_tree_by_element" +
                " WHERE deployment_id = ?" +
                " AND student_id = ?" +
                " AND root_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, studentId, rootId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public StudentScopeTrace fromRow(Row row) {
        return new StudentScopeTrace()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setStudentId(row.getUUID("student_id"))
                .setRootId(row.getUUID("root_id"))
                .setStudentScopeUrn(row.getUUID("scope_urn"))
                .setScopeId(row.getUUID("scope_id"))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
