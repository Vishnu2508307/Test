package com.smartsparrow.publication.job.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class NotificationByJobMutator extends SimpleTableMutator<NotificationByJob> {

    @Override
    public String getUpsertQuery(NotificationByJob mutation) {

        return "INSERT INTO publication.notification_by_job (" +
                "  job_id" +
                ", notification_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, NotificationByJob mutation) {
        stmt.setUUID(0, mutation.getJobId());
        stmt.setUUID(1, mutation.getNotificationId());
    }
}
