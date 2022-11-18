package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AssetRetryNotificationMutator extends SimpleTableMutator<AssetRetryNotification> {

    @Override
    public Statement upsert(AssetRetryNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO asset.retry_notification (" +
                "  notification_id" +
                ", delay_sec" +
                ", source_notification_id"
                + ") VALUES (?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final AssetRetryNotification mutation) {
        stmt.bind(mutation.getNotificationId(),
                mutation.getDelaySec(),
                mutation.getSourceNotificationId());
    }
}
