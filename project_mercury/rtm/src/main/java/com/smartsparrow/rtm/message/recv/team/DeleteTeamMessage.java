package com.smartsparrow.rtm.message.recv.team;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteTeamMessage extends ReceivedMessage implements TeamMessage, SubscriptionMessage {

    private UUID teamId;
    private UUID subscriptionId;

    @Override
    public UUID getTeamId() {
        return teamId;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteTeamMessage that = (DeleteTeamMessage) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, subscriptionId);
    }

    @Override
    public String toString() {
        return "DeleteTeamMessage{" +
                "teamId=" + teamId +
                ", subscriptionId='" + subscriptionId + '\'' +
                "} " + super.toString();
    }
}
