package com.smartsparrow.ext_http.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.ext_http.service.RetryNotification;
import com.smartsparrow.util.Enums;

class RetryNotificationMutator extends SimpleTableMutator<RetryNotification> {

    @Override
    public String getUpsertQuery(final RetryNotification mutation) {
        // @formatter:off
        return "INSERT INTO ext_http.retry_notification ("
                + "  notification_id"
                + ", source_notification_id"
                + ", reference_id"
                + ", purpose"
                + ", delay_sec"
                + ") VALUES ( ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final RetryNotification mutation) {
        stmt.bind(mutation.getState().getNotificationId(),
                  mutation.getSourceNotificationId(),
                  mutation.getState().getReferenceId(),
                  Enums.asString(mutation.getState().getPurpose()),
                  mutation.getDelaySec());


    }
}
