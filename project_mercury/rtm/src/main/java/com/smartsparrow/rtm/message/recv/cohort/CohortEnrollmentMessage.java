package com.smartsparrow.rtm.message.recv.cohort;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CohortEnrollmentMessage extends ReceivedMessage implements CohortMessage {

    private UUID cohortId;
    private UUID accountId;

    @Override
    public UUID getCohortId() {
        return cohortId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortEnrollmentMessage that = (CohortEnrollmentMessage) o;
        return Objects.equals(cohortId, that.cohortId) &&
                Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortId, accountId);
    }

    @Override
    public String toString() {
        return "CohortEnrollmentMessage{" +
                "cohortId=" + cohortId +
                ", accountId=" + accountId +
                '}';
    }
}
