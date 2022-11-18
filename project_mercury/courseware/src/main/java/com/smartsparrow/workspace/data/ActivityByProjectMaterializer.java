package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ActivityByProjectMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ActivityByProjectMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAllActivities(final UUID projectId) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " activity_id" +
                ", project_id" +
                " FROM workspace.activity_by_project" +
                " WHERE project_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public ProjectActivity fromRow(final Row row) {
        return new ProjectActivity()
                .setProjectId(row.getUUID("project_id"))
                .setActivityId(row.getUUID("activity_id"));
    }
}
