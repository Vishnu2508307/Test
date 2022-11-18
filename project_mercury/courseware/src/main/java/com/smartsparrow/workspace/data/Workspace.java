package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class Workspace {

    private UUID id;
    private UUID subscriptionId;
    private String name;
    private String description;

    public UUID getId() {
        return id;
    }

    public Workspace setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public Workspace setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Workspace setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Workspace setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Workspace workspace = (Workspace) o;
        return Objects.equals(id, workspace.id) && Objects.equals(subscriptionId, workspace.subscriptionId)
                && Objects.equals(name, workspace.name) && Objects.equals(description, workspace.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subscriptionId, name, description);
    }

    @Override
    public String toString() {
        return "Workspace{" + "id=" + id + ", subscriptionId=" + subscriptionId + ", name='" + name + '\''
                + ", description='" + description + '\'' + '}';
    }
}
