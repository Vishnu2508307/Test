package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ThemeByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;
    
    @Inject
    public ThemeByTeamMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByTeam(final UUID teamId) {

        // @formatter:off
        final String QUERY = "SELECT "
                + " theme_id"
                + " FROM workspace.theme_by_team"
                + " WHERE team_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("theme_id");
    }
}
