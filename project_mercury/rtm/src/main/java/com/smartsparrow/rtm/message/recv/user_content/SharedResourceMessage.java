package com.smartsparrow.rtm.message.recv.user_content;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.user_content.data.ResourceType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SharedResourceMessage extends ReceivedMessage {

    // the account identifier
    private UUID accountId;

    // the shared account id identifier
    private UUID sharedAccountId;

    //the resource(project/workspace) identifier
    private UUID resourceId;

    //the resource type
    private ResourceType resourceType;

    //course shared at datetime stamp
    private UUID sharedAt;

    public UUID getAccountId() {
        return accountId;
    }

    public SharedResourceMessage setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getSharedAccountId() {
        return sharedAccountId;
    }

    public SharedResourceMessage setSharedAccountId(final UUID sharedAccountId) {
        this.sharedAccountId = sharedAccountId;
        return this;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public SharedResourceMessage setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public SharedResourceMessage setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public UUID getSharedAt() {
        return sharedAt;
    }

    public SharedResourceMessage setSharedAt(final UUID sharedAt) {
        this.sharedAt = sharedAt;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedResourceMessage that = (SharedResourceMessage) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(sharedAccountId,
                                                                           that.sharedAccountId) && Objects.equals(
                resourceId,
                that.resourceId) && resourceType == that.resourceType && Objects.equals(sharedAt, that.sharedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, sharedAccountId, resourceId, resourceType, sharedAt);
    }

    @Override
    public String toString() {
        return "SharedResourceMessage{" +
                "accountId=" + accountId +
                ", sharedAccountId=" + sharedAccountId +
                ", resourceId=" + resourceId +
                ", resourceType=" + resourceType +
                ", sharedAt=" + sharedAt +
                '}';
    }
}
