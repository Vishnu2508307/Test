package com.smartsparrow.courseware.data;

import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.payload.PluginRefPayload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ActivitySummary {

    private UUID activityId;
    private List<ConfigurationField> configFields;
    private AccountPayload creator;
    private String createdAt;
    private String updatedAt;
    private PluginRefPayload plugin;


    public UUID getActivityId() {
        return activityId;
    }

    public ActivitySummary setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public List<ConfigurationField> getConfigFields() {
        return configFields;
    }

    public ActivitySummary setConfigFields(List<ConfigurationField> configFields) {
        this.configFields = configFields;
        return this;
    }

    public AccountPayload getCreator() {
        return creator;
    }

    public ActivitySummary setCreator(AccountPayload creator) {
        this.creator = creator;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ActivitySummary setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public ActivitySummary setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public PluginRefPayload getPlugin() {
        return plugin;
    }

    public ActivitySummary setPlugin(PluginRefPayload plugin) {
        this.plugin = plugin;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivitySummary that = (ActivitySummary) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(configFields, that.configFields) &&
                Objects.equals(creator, that.creator) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(plugin, that.plugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, configFields, creator, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ActivitySummary{" +
                "activityId=" + activityId +
                ", configFields='" + configFields + '\'' +
                ", creator=" + creator +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", plugin='" + plugin + '\'' +
                '}';
    }
}
