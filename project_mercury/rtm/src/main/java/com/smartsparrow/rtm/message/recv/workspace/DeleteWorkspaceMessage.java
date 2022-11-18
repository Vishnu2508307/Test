package com.smartsparrow.rtm.message.recv.workspace;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteWorkspaceMessage extends ReceivedMessage implements WorkspaceMessage, SubscriptionMessage {

    private UUID workspaceId;
    private String name;
    private UUID subscriptionId;

    @Override
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteWorkspaceMessage that = (DeleteWorkspaceMessage) o;
        return Objects.equals(workspaceId, that.workspaceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, name, subscriptionId);
    }

    @Override
    public String toString() {
        return "DeleteWorkspaceMessage{" +
                "workspaceId='" + workspaceId + '\'' +
                ", name='" + name + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                "} " + super.toString();
    }
}
