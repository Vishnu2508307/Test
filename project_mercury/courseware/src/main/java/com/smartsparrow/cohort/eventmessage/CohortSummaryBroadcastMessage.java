package com.smartsparrow.cohort.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.cohort.data.CohortSummary;

public class CohortSummaryBroadcastMessage extends CohortBroadcastMessage {

    private static final long serialVersionUID = 2449697479924146441L;

    private CohortSummary cohortSummary;

    public CohortSummaryBroadcastMessage(UUID cohortId) {
        super(cohortId);
    }

    public CohortSummary getCohortSummary() {
        return cohortSummary;
    }

    public CohortSummaryBroadcastMessage setCohortSummary(CohortSummary cohortSummary) {
        this.cohortSummary = cohortSummary;
        return this;
    }

    @Override
    public UUID getCohortId() {
        return cohortSummary.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSummaryBroadcastMessage that = (CohortSummaryBroadcastMessage) o;
        return Objects.equals(cohortSummary, that.cohortSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortSummary);
    }

    @Override
    public String toString() {
        return "CohortSummaryBroadcastMessage{" +
                "cohortSummary=" + cohortSummary +
                '}';
    }
}
