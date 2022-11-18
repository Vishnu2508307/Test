package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

import javax.inject.Inject;
import java.util.UUID;

class AlfrescoAssetSyncNotificationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AlfrescoAssetSyncNotificationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID notificationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                " notification_id" +
                ", reference_id" +
                ", course_id" +
                ", asset_id" +
                ", sync_type" +
                ", status" +
                ", completed_at" +
                "  FROM asset.alfresco_sync_notification" +
                "  WHERE notification_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(notificationId);
        return stmt;
    }

    public AlfrescoAssetSyncNotification fromRow(Row row) {
        return new AlfrescoAssetSyncNotification()
                .setNotificationId(row.getUUID("notification_id"))
                .setReferenceId(row.getUUID("reference_id"))
                .setCourseId(row.getUUID("course_id"))
                .setAssetId(row.getUUID("asset_id"))
                .setSyncType(Enums.of(AlfrescoAssetSyncType.class, row.getString("sync_type")))
                .setStatus(Enums.of(AlfrescoAssetSyncStatus.class, row.getString("status")))
                .setCompletedAt(row.getUUID("completed_at"));
    }

}
