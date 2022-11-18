package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class NotificationMutator extends SimpleTableMutator<Notification> {

    @Override
    public String getUpsertQuery(Notification mutation) {

        return "INSERT INTO publication.notification (" +
                "  id" +
                ", notification_type" +
                ", status" +
                ", message"
                + ") VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Notification mutation) {
        stmt.setUUID(0, mutation.getId());
        stmt.setString(1, Enums.asString(mutation.getNotificationType()));
        stmt.setString(2, Enums.asString(mutation.getNotificationStatus()));
        stmt.setString(3, mutation.getMessage());
    }
}
