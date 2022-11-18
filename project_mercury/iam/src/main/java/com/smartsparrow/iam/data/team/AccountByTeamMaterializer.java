package com.smartsparrow.iam.data.team;

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

public class AccountByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    AccountByTeamMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchByTeamId(final UUID teamId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  team_id"
                + ", account_id"
                + ", permission_level"
                + " FROM team.account_by_team"
                + " WHERE team_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(teamId);
        return stmt;
    }

    /**
     * Converts a cassandra row to AccountTeamCollaborator type
     *
     * @param row {@link Row}
     * @return AccountTeamCollaborator {@link AccountTeamCollaborator}
     */
    protected AccountTeamCollaborator fromRow(Row row) {
        return new AccountTeamCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setAccountId(row.getUUID("account_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
