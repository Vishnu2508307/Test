package com.smartsparrow.iam.data.team;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class TeamByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    TeamByAccountMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchByAccountId(final UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  account_id"
                + ", team_id"
                + " FROM team.team_by_account"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId);
        return stmt;
    }

    /**
     * Converts a cassandra row to TeamAccount
     *
     * @param row {@link Row}
     * @return teamAccount {@link TeamAccount}
     */
    protected TeamAccount fromRow(Row row) {
        return new TeamAccount()
                .setAccountId(row.getUUID("account_id"))
                .setTeamId(row.getUUID("team_id"));
    }
}
