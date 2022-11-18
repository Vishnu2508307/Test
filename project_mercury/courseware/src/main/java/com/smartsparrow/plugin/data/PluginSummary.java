package com.smartsparrow.plugin.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;

public class PluginSummary {

    private UUID id;
    private UUID subscriptionId;
    private String name;
    private UUID creatorId;
    private PluginType type;
    private String description;
    private UUID deletedId;
    private String latestGuide;
    /**
     * Defines publishing mode either DEFAULT or STRICT
     */
    private PublishMode publishMode;

    /**
     * Latest stable version or latest unstable if only unstable versions were published.
     * If it is empty - no versions were published yet
     */
    private String latestVersion;
    private Long latestVersionReleaseDate;

    private String thumbnail;
    private List<String> tags;
    private String defaultHeight;

    public UUID getId() {
        return id;
    }

    public PluginSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public PluginSummary setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginSummary setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public PluginSummary setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public PluginType getType() {
        return type;
    }

    public PluginSummary setType(PluginType type) {
        this.type = type;
        return this;
    }

    public PublishMode getPublishMode() {
        if(publishMode == null) {
            return PublishMode.DEFAULT;
        }
        return publishMode;
    }

    public PluginSummary setPublishMode(final PublishMode publishMode) {
        this.publishMode = publishMode;
        return this;
    }

    /**
     * see {@link PluginSummary#latestVersion}
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    public PluginSummary setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PluginSummary setDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getLatestVersionReleaseDate() {
        return latestVersionReleaseDate;
    }

    public PluginSummary setLatestVersionReleaseDate(Long latestVersionReleaseDate) {
        this.latestVersionReleaseDate = latestVersionReleaseDate;
        return this;
    }

    public UUID getDeletedId() {
        return deletedId;
    }

    public PluginSummary setDeletedId(UUID deletedId) {
        this.deletedId = deletedId;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public PluginSummary setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public PluginSummary setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @JsonIgnore
    public boolean isPublished() {
        return latestVersion != null;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deletedId != null;
    }

    @Nullable
    public String getLatestGuide() {
        return latestGuide;
    }

    public PluginSummary setLatestGuide(String latestGuide) {
        this.latestGuide = latestGuide;
        return this;
    }

    @Nullable
    public String getDefaultHeight() {
        return defaultHeight;
    }

    public PluginSummary setDefaultHeight(final String defaultHeight) {
        this.defaultHeight = defaultHeight;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginSummary that = (PluginSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(creatorId, that.creatorId) &&
                type == that.type &&
                Objects.equals(description, that.description) &&
                Objects.equals(deletedId, that.deletedId) &&
                Objects.equals(latestGuide, that.latestGuide) &&
                publishMode == that.publishMode &&
                Objects.equals(latestVersion, that.latestVersion) &&
                Objects.equals(latestVersionReleaseDate, that.latestVersionReleaseDate) &&
                Objects.equals(thumbnail, that.thumbnail) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(defaultHeight, that.defaultHeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subscriptionId, name, creatorId, type, description, deletedId, latestGuide,
                            publishMode, latestVersion, latestVersionReleaseDate, thumbnail, tags, defaultHeight);
    }

    @Override
    public String toString() {
        return "PluginSummary{" +
                "id=" + id +
                ", subscriptionId=" + subscriptionId +
                ", name='" + name + '\'' +
                ", creatorId=" + creatorId +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", deletedId=" + deletedId +
                ", latestGuide='" + latestGuide + '\'' +
                ", publishMode=" + publishMode +
                ", latestVersion='" + latestVersion + '\'' +
                ", latestVersionReleaseDate=" + latestVersionReleaseDate +
                ", thumbnail='" + thumbnail + '\'' +
                ", tags=" + tags +
                ", defaultHeight=" + defaultHeight +
                '}';
    }
}
