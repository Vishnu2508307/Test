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

public class StudentScopeRegistryByCoursewareElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public StudentScopeRegistryByCoursewareElementMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement find(UUID elementId, UUID studentScopeURN) {
        String SELECT = "SELECT" +
                " element_id" +
                ", student_scope_urn" +
                ", element_type" +
                ", plugin_id" +
                ", plugin_version FROM courseware.student_scope_registry_by_courseware_element" +
                " WHERE element_id = ?" +
                " AND student_scope_urn = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId, studentScopeURN);
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
