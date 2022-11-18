package com.smartsparrow.iam.data.permission.subscription;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;
import com.smartsparrow.iam.service.PermissionLevel;

public class TeamSubscriptionCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public TeamSubscriptionCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchCollaborators(UUID subscriptionId) {

        final String SELECT_BY_SUBSCRIPTION = "SELECT " +
                "subscription_id, " +
                "team_id, " +
                "permission_level " +
                "FROM subscription.team_by_subscription " +
                "WHERE subscription_id = ?";
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_SUBSCRIPTION);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public SubscriptionTeamCollaborator fromRow(Row row) {
        return new SubscriptionTeamCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setPermissionLevel(Enum.valueOf(PermissionLevel.class, row.getString("permission_level")));
    }
}
