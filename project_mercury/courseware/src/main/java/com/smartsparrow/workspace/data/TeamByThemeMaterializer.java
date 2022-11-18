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

class TeamByThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public TeamByThemeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String BY_THEME = "SELECT " +
            "theme_id, " +
            "team_id, " +
            "permission_level " +
            "FROM workspace.team_by_theme " +
            "WHERE theme_id = ?";

    @SuppressWarnings("Duplicates")
    public Statement fetchByTheme(UUID themeId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_THEME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(themeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByThemeTeam(UUID themeId, UUID teamId) {
        final String BY_WORKSPACE_TEAM = BY_THEME + " AND team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_WORKSPACE_TEAM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(themeId, teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public TeamByTheme fromRow(Row row) {
        return new TeamByTheme()
                .setThemeId(row.getUUID("theme_id"))
                .setTeamId(row.getUUID("team_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
