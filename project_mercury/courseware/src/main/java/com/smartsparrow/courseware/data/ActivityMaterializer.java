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

class ActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ActivityMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ", creator_id"
                + ", student_scope_urn"
                + ", evaluation_mode"
                + " FROM courseware.activity"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    /**
     * Maps a row to an activity
     * @param row to be converted
     * @return materialized activity object built from the row
     */
    public Activity fromRow(Row row) {
        return new Activity() //
                .setId(row.getUUID("id")) //
                .setPluginId(row.getUUID("plugin_id")) //
                .setPluginVersionExpr(row.getString("plugin_version_expr"))
                .setCreatorId(row.getUUID("creator_id"))
                .setStudentScopeURN(row.getUUID("student_scope_urn"))
                .setEvaluationMode(Enums.of(EvaluationMode.class, row.getString("evaluation_mode")));
    }
}
