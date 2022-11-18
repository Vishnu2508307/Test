package com.smartsparrow.iam.data.permission.workspace;

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

public class ThemePermissionByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ThemePermissionByTeamMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchThemePermissionByTeam(UUID teamId, UUID themeId) {
        final String QUERY = "SELECT " +
                "permission_level " +
                "FROM iam_global.theme_permission_by_team " +
                "WHERE team_id = ?" +
                "AND theme_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId, themeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public PermissionLevel fromRow(Row row) {
        return Enums.of(PermissionLevel.class, row.getString("permission_level"));
    }
}
