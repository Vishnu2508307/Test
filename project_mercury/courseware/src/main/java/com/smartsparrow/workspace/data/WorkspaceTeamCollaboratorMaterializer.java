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

public class WorkspaceTeamCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public WorkspaceTeamCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String BY_WORKSPACE = "SELECT " +
            "workspace_id, " +
            "team_id, " +
            "permission_level " +
            "FROM workspace.team_by_workspace " +
            "WHERE workspace_id = ?";

    @SuppressWarnings("Duplicates")
    public Statement fetchByWorkspace(UUID workspaceId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_WORKSPACE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByWorkspaceTeam(UUID workspaceId, UUID teamId) {
        final String BY_WORKSPACE_TEAM = BY_WORKSPACE + " AND team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_WORKSPACE_TEAM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(workspaceId, teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public WorkspaceTeamCollaborator fromRow(Row row) {
        return new WorkspaceTeamCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
