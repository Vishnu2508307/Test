package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AssumeTokenMessage extends ReceivedMessage {

    private UUID accountId;

    public AssumeTokenMessage() {
    }

    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssumeTokenMessage that = (AssumeTokenMessage) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "AssumeTokenMessage{" +
                "accountId=" + accountId +
                "} " + super.toString();
    }

}
