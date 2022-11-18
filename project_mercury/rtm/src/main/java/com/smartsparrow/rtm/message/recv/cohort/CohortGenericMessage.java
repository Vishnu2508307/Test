package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CohortGenericMessage extends ReceivedMessage implements CohortMessage {

    private UUID cohortId;

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortGenericMessage that = (CohortGenericMessage) o;
        return Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId);
    }

    @Override
    public String toString() {
        return "CohortGenericMessage{" +
                "cohortId=" + cohortId +
                '}';
    }
}
