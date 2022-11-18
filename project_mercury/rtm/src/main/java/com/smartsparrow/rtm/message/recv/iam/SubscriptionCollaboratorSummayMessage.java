package com.smartsparrow.rtm.message.recv.iam;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SubscriptionCollaboratorSummayMessage extends ReceivedMessage implements SubscriptionMessage {

    private UUID subscriptionId;
    private Integer limit;

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionCollaboratorSummayMessage that = (SubscriptionCollaboratorSummayMessage) o;
        return Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, limit);
    }

    @Override
    public String toString() {
        return "SubscriptionCollaboratorSummayMessage{" +
                "subscriptionId=" + subscriptionId +
                ", limit=" + limit +
                '}';
    }
}
