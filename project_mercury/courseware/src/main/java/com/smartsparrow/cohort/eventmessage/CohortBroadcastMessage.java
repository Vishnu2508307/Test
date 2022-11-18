package com.smartsparrow.cohort.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class CohortBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -5515631302287755502L;
    public final UUID cohortId;

    public CohortBroadcastMessage(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortBroadcastMessage that = (CohortBroadcastMessage) o;
        return Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId);
    }

    @Override
    public String toString() {
        return "CohortBroadcastMessage{" +
                "cohortId=" + cohortId +
                '}';
    }
}
