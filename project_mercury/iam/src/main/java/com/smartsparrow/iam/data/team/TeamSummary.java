package com.smartsparrow.iam.data.team;

import java.util.Objects;
import java.util.UUID;

public class TeamSummary {

    private UUID id;
    private UUID subscriptionId;
    private String name;
    private String description;
    private String thumbnail;

    public UUID getId() {
        return id;
    }

    public TeamSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public TeamSummary setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getName() {
        return name;
    }

    public TeamSummary setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TeamSummary setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public TeamSummary setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamSummary that = (TeamSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(thumbnail, that.thumbnail);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, subscriptionId, name, description, thumbnail);
    }

    @Override
    public String toString() {
        return "TeamSummary{" +
                "id=" + id +
                ", subscriptionId=" + subscriptionId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
