package com.smartsparrow.iam.data.team;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class TeamSummaryMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    TeamSummaryMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchTeamSummaryByTeam(final UUID teamId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  id"
                + ", subscription_id"
                + ", name"
                + ", description"
                + ", thumbnail"
                + " FROM team.team_summary"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(teamId);
        return stmt;
    }

    /**
     * Converts a cassandra row to TeamSummary
     *
     * @param row {@link Row}
     * @return TeamSummary {@link TeamSummary}
     */
    protected TeamSummary fromRow(Row row) {
        return new TeamSummary()
                .setId(row.getUUID("id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setName(row.getString("name"))
                .setDescription(row.getString("description"))
                .setThumbnail(row.getString("thumbnail"));
    }

}
