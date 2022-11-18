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

public class InteractiveMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    InteractiveMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID interactiveId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ", student_scope_urn"
                + ", evaluation_mode"
                + " FROM courseware.interactive"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(interactiveId);
        return stmt;
    }

    public Interactive fromRow(final Row row) {
        return new Interactive()
                .setId(row.getUUID("id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"))
                .setStudentScopeURN(row.getUUID("student_scope_urn"))
                .setEvaluationMode(Enums.of(EvaluationMode.class, row.getString("evaluation_mode")));
    }
}
