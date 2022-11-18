package com.smartsparrow.math.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AssetRetryNotificationMutator extends SimpleTableMutator<MathAssetRetryNotification> {

    @Override
    public Statement upsert(MathAssetRetryNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO math.retry_notification (" +
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
    public void bindUpsert(final BoundStatement stmt, final MathAssetRetryNotification mutation) {
        stmt.bind(mutation.getNotificationId(),
                mutation.getDelaySec(),
                mutation.getSourceNotificationId());
    }
}
