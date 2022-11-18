package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class AlfrescoAssetSyncNotificationMutator extends SimpleTableMutator<AlfrescoAssetSyncNotification> {

    @Override
    public Statement upsert(AlfrescoAssetSyncNotification mutation) {

        // @formatter:off
        String QUERY = "INSERT INTO asset.alfresco_sync_notification (" +
                " notification_id" +
                ", reference_id" +
                ", course_id" +
                ", asset_id" +
                ", sync_type" +
                ", status" +
                ", completed_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        bindUpsert(stmt, mutation);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public void bindUpsert(BoundStatement stmt, AlfrescoAssetSyncNotification mutation) {
        stmt.setUUID(0, mutation.getNotificationId());
        stmt.setUUID(1, mutation.getReferenceId());
        stmt.setUUID(2, mutation.getCourseId());
        stmt.setUUID(3, mutation.getAssetId());
        stmt.setString(4, Enums.asString(mutation.getSyncType()));
        stmt.setString(5, Enums.asString(mutation.getStatus()));
        Mutators.bindNonNull(stmt, 6, mutation.getCompletedAt());
    }

}
