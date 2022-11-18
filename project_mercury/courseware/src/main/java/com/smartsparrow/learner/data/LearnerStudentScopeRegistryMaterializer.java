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

public class LearnerStudentScopeRegistryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerStudentScopeRegistryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String QUERY_ALL = "SELECT" +
            " student_scope_urn" +
            ", deployment_id" +
            ", element_id" +
            ", change_id" +
            ", element_type" +
            ", plugin_id" +
            ", plugin_version FROM learner.student_scope_registry" +
            " WHERE student_scope_urn = ?" +
            " AND deployment_id = ?" +
            " AND change_id = ?";

    public Statement findRegisteredElement(UUID studentScopeURN, UUID deploymentId, UUID changeId, UUID elementId) {
        final String QUERY = QUERY_ALL +
                " AND element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentScopeURN, deploymentId, changeId, elementId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findAllRegistered(UUID studentScopeURN, UUID deploymentId, UUID changeId) {
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY_ALL);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(studentScopeURN, deploymentId, changeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerScopeReference fromRow(Row row) {
        return new LearnerScopeReference()
                .setChangeId(row.getUUID("change_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setScopeURN(row.getUUID("student_scope_urn"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersion(row.getString("plugin_version"));
    }
}
