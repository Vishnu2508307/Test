package com.smartsparrow.user_content.data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared Project & Workspace are tracked against account id
 */
public class SharedResource implements Serializable {

    private UUID id;

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

    public UUID getId() {
        return id;
    }

    public SharedResource setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public SharedResource setAccountId(final UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public UUID getSharedAccountId() {
        return sharedAccountId;
    }

    public SharedResource setSharedAccountId(final UUID sharedAccountId) {
        this.sharedAccountId = sharedAccountId;
        return this;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public SharedResource setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public SharedResource setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public UUID getSharedAt() {
        return sharedAt;
    }

    public SharedResource setSharedAt(final UUID sharedAt) {
        this.sharedAt = sharedAt;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedResource that = (SharedResource) o;
        return Objects.equals(id, that.id) && Objects.equals(accountId,
                                                             that.accountId) && Objects.equals(
                sharedAccountId,
                that.sharedAccountId) && Objects.equals(resourceId,
                                                        that.resourceId) && resourceType == that.resourceType && Objects.equals(
                sharedAt,
                that.sharedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, sharedAccountId, resourceId, resourceType, sharedAt);
    }

    @Override
    public String toString() {
        return "SharedResource{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", sharedAccountId=" + sharedAccountId +
                ", resourceId=" + resourceId +
                ", resourceType=" + resourceType +
                ", sharedAt=" + sharedAt +
                '}';
    }
}
