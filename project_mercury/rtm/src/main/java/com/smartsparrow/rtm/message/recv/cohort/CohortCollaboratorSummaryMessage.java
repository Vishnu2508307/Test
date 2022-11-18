package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CohortCollaboratorSummaryMessage extends ReceivedMessage implements CohortMessage {

    private Integer limit;
    private UUID cohortId;

    public Integer getLimit() {
        return limit;
    }

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortCollaboratorSummaryMessage that = (CohortCollaboratorSummaryMessage) o;
        return Objects.equals(limit, that.limit) &&
                Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(limit, cohortId);
    }

    @Override
    public String toString() {
        return "CohortCollaboratorSummaryMessage{" +
                "limit=" + limit +
                ", cohortId=" + cohortId +
                '}';
    }
}
