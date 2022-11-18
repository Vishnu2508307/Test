package com.smartsparrow.la.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class EventSummaryMutator extends SimpleTableMutator<EventSummary> {

    @Override
    public String getUpsertQuery(EventSummary mutation) {
        return "INSERT INTO analytics_event.event_summary (" +
                "id" +
                ", namespace" +
                ", message_type_code" +
                ", version" +
                ", stream_type" +
                ", create_type" +
                ", correlation_id" +
                ", payload" +
                ", tags" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, EventSummary mutation) {
        stmt.bind(mutation.getId(),
                mutation.getNamespace(),
                mutation.getMessageTypeCode(),
                mutation.getVersion(),
                mutation.getStreamType(),
                mutation.getCreateType(),
                mutation.getCorrelationId(),
                mutation.getPayload(),
                mutation.getTags());
    }
}
