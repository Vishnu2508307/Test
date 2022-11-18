package com.smartsparrow.plugin.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.util.DateFormat;

public class PluginSummaryPayload {

    private UUID pluginId;
    private String name;
    private String description;
    private PluginType type;
    private String latestVersion;
    private UUID subscriptionId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private AccountPayload accountPayload;
    private String thumbnail;
    private List<String> tags;
    private String latestGuide;
    private PublishMode publishMode;
    private String defaultHeight;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginSummaryPayload setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginSummaryPayload setName(String name) {
        this.name = name;
        return this;
    }

    public PluginType getType() {
        return type;
    }

    public PluginSummaryPayload setType(PluginType type) {
        this.type = type;
        return this;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public PluginSummaryPayload setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public PluginSummaryPayload setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PluginSummaryPayload setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public PluginSummaryPayload setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public PluginSummaryPayload setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public PluginSummaryPayload setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public PluginSummaryPayload setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public PluginSummaryPayload setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @Nullable
    public String getLatestGuide() {
        return latestGuide;
    }

    public PluginSummaryPayload setLatestGuide(String latestGuide) {
        this.latestGuide = latestGuide;
        return this;
    }

    public PublishMode getPublishMode() { return publishMode; }

    public PluginSummaryPayload setPublishMode(final PublishMode publishMode) {
        this.publishMode = publishMode;
        return this;
    }

    @Nullable
    public String getDefaultHeight() {
        return defaultHeight;
    }

    public PluginSummaryPayload setDefaultHeight(final String defaultHeight) {
        this.defaultHeight = defaultHeight;
        return this;
    }

    @JsonIgnore
    public static PluginSummaryPayload from(@Nonnull PluginSummary pluginSummary) {
        return new PluginSummaryPayload()
                .setPluginId(pluginSummary.getId())
                .setName(pluginSummary.getName())
                .setDescription(pluginSummary.getDescription())
                .setType(pluginSummary.getType())
                .setSubscriptionId(pluginSummary.getSubscriptionId())
                .setLatestVersion(pluginSummary.getLatestVersion())
                .setCreatedAt(DateFormat.asRFC1123(pluginSummary.getId()))
                .setUpdatedAt(pluginSummary.getLatestVersionReleaseDate() == null ?
                                      null : DateFormat.asRFC1123(pluginSummary.getLatestVersionReleaseDate()))
                .setDeletedAt(pluginSummary.getDeletedId() == null ? null : DateFormat.asRFC1123(pluginSummary.getDeletedId()))
                .setThumbnail(pluginSummary.getThumbnail())
                .setTags(pluginSummary.getTags())
                .setLatestGuide(pluginSummary.getLatestGuide())
                .setPublishMode(pluginSummary.getPublishMode())
                .setDefaultHeight(pluginSummary.getDefaultHeight());
    }

    @JsonIgnore
    public static PluginSummaryPayload from(@Nonnull PluginSummary pluginSummary, @Nonnull AccountPayload accountPayload) {
        return new PluginSummaryPayload()
                .setPluginId(pluginSummary.getId())
                .setName(pluginSummary.getName())
                .setDescription(pluginSummary.getDescription())
                .setType(pluginSummary.getType())
                .setSubscriptionId(pluginSummary.getSubscriptionId())
                .setLatestVersion(pluginSummary.getLatestVersion())
                .setCreatedAt(DateFormat.asRFC1123(pluginSummary.getId()))
                .setUpdatedAt(pluginSummary.getLatestVersionReleaseDate() == null ?
                        null : DateFormat.asRFC1123(pluginSummary.getLatestVersionReleaseDate()))
                .setAccountPayload(accountPayload.getAccountId() == null ? null : accountPayload)
                .setDeletedAt(pluginSummary.getDeletedId() == null ? null : DateFormat.asRFC1123(pluginSummary.getDeletedId()))
                .setThumbnail(pluginSummary.getThumbnail())
                .setTags(pluginSummary.getTags())
                .setLatestGuide(pluginSummary.getLatestGuide())
                .setPublishMode(pluginSummary.getPublishMode())
                .setDefaultHeight(pluginSummary.getDefaultHeight());
    }

    @JsonProperty("creator")
    public AccountPayload getAccountPayload() {
        return accountPayload;
    }

    @JsonProperty("creator")
    public PluginSummaryPayload setAccountPayload(AccountPayload accountPayload) {
        this.accountPayload = accountPayload;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginSummaryPayload that = (PluginSummaryPayload) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                type == that.type &&
                Objects.equals(latestVersion, that.latestVersion) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(deletedAt, that.deletedAt) &&
                Objects.equals(accountPayload, that.accountPayload) &&
                Objects.equals(thumbnail, that.thumbnail) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(latestGuide, that.latestGuide) &&
                publishMode == that.publishMode &&
                Objects.equals(defaultHeight, that.defaultHeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, name, description, type, latestVersion, subscriptionId, createdAt, updatedAt,
                            deletedAt, accountPayload, thumbnail, tags, latestGuide, publishMode, defaultHeight);
    }

    @Override
    public String toString() {
        return "PluginSummaryPayload{" +
                "pluginId=" + pluginId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", latestVersion='" + latestVersion + '\'' +
                ", subscriptionId=" + subscriptionId +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", deletedAt='" + deletedAt + '\'' +
                ", accountPayload=" + accountPayload +
                ", thumbnail='" + thumbnail + '\'' +
                ", tags=" + tags +
                ", latestGuide='" + latestGuide + '\'' +
                ", publishMode=" + publishMode +
                ", defaultHeight=" + defaultHeight +
                '}';
    }
}
