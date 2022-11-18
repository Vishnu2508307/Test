package com.smartsparrow.la.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class EventTrackingMutator extends SimpleTableMutator<EventTracking> {

    @Override
    public String getUpsertQuery(EventTracking mutation) {
        return "INSERT INTO analytics_event.tracking_by_event (" +
                "event_id" +
                ", tracking_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, EventTracking mutation) {
        stmt.bind(mutation.getEventId(), mutation.getTrackingId());
    }

}
