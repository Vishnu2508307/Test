package com.smartsparrow.ext_http.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.ext_http.service.NotificationState;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.util.Enums;

class RequestNotificationMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    RequestNotificationMaterializer(final PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement findById(UUID notificationId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  notification_id"
                + ", reference_id"
                + ", purpose"
                + ", params"
                + " FROM ext_http.request_notification"
                + " WHERE notification_id=?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(notificationId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public RequestNotification fromRow(Row row) {
        return new RequestNotification() //
                .setState(new NotificationState()
                                  .setNotificationId(row.getUUID("notification_id"))
                                  .setReferenceId(row.getUUID("reference_id"))
                                  .setPurpose(Enums.of(RequestPurpose.class, row.getString("purpose"))))
                .setParamsFromJson(row.getString("params"));
    }

}
