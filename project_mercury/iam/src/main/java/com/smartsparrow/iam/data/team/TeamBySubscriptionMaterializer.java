package com.smartsparrow.iam.data.team;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class TeamBySubscriptionMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    TeamBySubscriptionMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchBySubscriptionId(final UUID subscriptionId) {
        // @formatter:off
        final String QUERY = "SELECT"
                + "  subscription_id"
                + ", team_id"
                + " FROM team.team_by_subscription"
                + " WHERE subscription_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(subscriptionId);
        return stmt;
    }

    /**
     * Converts a cassandra row to TeamBySubscription
     *
     * @param row {@link Row}
     * @return TeamBySubscription {@link TeamBySubscription}
     */
    protected TeamBySubscription fromRow(Row row) {
        return new TeamBySubscription()
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setTeamId(row.getUUID("team_id"));
    }

}
