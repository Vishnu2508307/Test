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

public class StudentScopeRegistryMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public StudentScopeRegistryMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String BY_SCOPE = "SELECT" +
            " student_scope_urn" +
            ", element_id" +
            ", element_type" +
            ", plugin_id" +
            ", plugin_version FROM courseware.student_scope_registry" +
            " WHERE student_scope_urn = ?";

    @SuppressWarnings("Duplicates")
    public Statement find(UUID studentScopeURN, UUID elementId) {
        String SELECT = BY_SCOPE + " AND element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(studentScopeURN, elementId);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public Statement findAllRegistered(UUID studentScopeURN) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_SCOPE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(studentScopeURN);
        return stmt;
    }

    public ScopeReference fromRow(Row row) {
        return new ScopeReference()
                .setElementId(row.getUUID("element_id"))
                .setScopeURN(row.getUUID("student_scope_urn"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersion(row.getString("plugin_version"));
    }
}
