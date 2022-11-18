package com.smartsparrow.la.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class EventFailureMutator extends SimpleTableMutator<EventFailure> {

    @Override
    public String getUpsertQuery(EventFailure mutation) {
        return "INSERT INTO analytics_event.failure_by_event (" +
                "event_id" +
                ", fail_id" +
                ", exception_message" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, EventFailure mutation) {
        stmt.bind(mutation.getEventId(), mutation.getFailId(), mutation.getExceptionMessage());
    }

    @Override
    public String getDeleteQuery(EventFailure mutation) {
        return "DELETE FROM analytics_event.failure_by_event WHERE " +
                "event_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, EventFailure mutation) {
        stmt.bind(mutation.getEventId());
    }
}
