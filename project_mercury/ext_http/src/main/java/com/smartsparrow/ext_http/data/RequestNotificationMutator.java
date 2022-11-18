package com.smartsparrow.ext_http.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.util.Enums;

class RequestNotificationMutator extends SimpleTableMutator<RequestNotification> {

    @Override
    public String getUpsertQuery(final RequestNotification mutation) {
        // @formatter:off
        return "INSERT INTO ext_http.request_notification ("
                + "  notification_id"
                + ", reference_id"
                + ", purpose"
                + ", params"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final RequestNotification mutation) {
        stmt.bind(mutation.getState().getNotificationId(), //
                  mutation.getState().getReferenceId(), //
                  Enums.asString(mutation.getState().getPurpose()), //
                  mutation.getParamsAsJson());
    }

}
