package com.smartsparrow.ext_http.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord;
import com.smartsparrow.util.Enums;

class ExternalHttpRequestLogRecordMutator extends SimpleTableMutator<ExternalHttpRequestLogRecord> {

    @Override
    public String getUpsertQuery(final ExternalHttpRequestLogRecord mutation) {
        // @formatter:off
        return "INSERT INTO ext_http.request_log ("
                + "  id"
                + ", notification_id"
                + ", event"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ExternalHttpRequestLogRecord mutation) {
        stmt.bind(mutation.getId(), mutation.getNotificationId(), Enums.asString(mutation.getEvent()));
    }

}
