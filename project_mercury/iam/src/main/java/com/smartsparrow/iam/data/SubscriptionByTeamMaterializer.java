package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class SubscriptionByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public SubscriptionByTeamMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchSubscriptions(UUID teamId) {
        final String SELECT_BY_TEAM = "SELECT " +
                "subscription_id " +
                "FROM subscription.subscription_by_team " +
                "WHERE team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_TEAM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("subscription_id");
    }
}
