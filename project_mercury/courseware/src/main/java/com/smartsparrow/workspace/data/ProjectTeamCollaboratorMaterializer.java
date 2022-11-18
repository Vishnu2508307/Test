package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class ProjectTeamCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ProjectTeamCollaboratorMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchTeamsForProject(final UUID projectId) {
        // @formatter:off
        final String QUERY = "SELECT" +
                " project_id" +
                ", team_id" +
                ", permission_level" +
                " FROM workspace.team_by_project" +
                " WHERE project_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(projectId);
        return stmt;
    }

    public ProjectTeamCollaborator fromRow(final Row row) {
        return new ProjectTeamCollaborator()
                .setProjectId(row.getUUID("project_id"))
                .setTeamId(row.getUUID("team_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
