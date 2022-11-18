package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.publication.job.enums.ArtifactType;
import com.smartsparrow.publication.job.enums.NotificationStatus;
import com.smartsparrow.publication.job.enums.NotificationType;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

class NotificationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public NotificationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID notificationId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  id" +
                ", notification_type" +
                ", status" +
                ", message" +
                " FROM publication.notification" +
                " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(notificationId);
        return stmt;
    }

    public Notification fromRow(Row row) {
        return new Notification()
                .setId(row.getUUID("id"))
                .setNotificationType(getNullableEnum(row, "notification_type", NotificationType.class))
                .setNotificationStatus(getNullableEnum(row, "status", NotificationStatus.class))
                .setMessage(row.getString("message"));
    }
}
