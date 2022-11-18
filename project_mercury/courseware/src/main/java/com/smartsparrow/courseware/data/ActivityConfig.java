package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.google.common.base.Objects;

/**
 * Represents the configuration of the plugin that is part of a an {@link Activity}
 */
public class ActivityConfig {

    private UUID id;
    private UUID activityId;
    private String config;

    public ActivityConfig() {
    }

    public UUID getId() {
        return id;
    }

    public ActivityConfig setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityConfig setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    /**
     * String representation of the Activity's plugin configuration. Must be validated against the plugin schema
     */
    public String getConfig() {
        return config;
    }

    /**
     * String representation of the Activity's plugin configuration. Must be validated against the plugin schema
     *
     * @param config the validated plugin configuration
     *
     */
    public ActivityConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActivityConfig that = (ActivityConfig) o;
        return Objects.equal(id, that.id) && Objects.equal(activityId, that.activityId) && Objects.equal(config,
                                                                                                         that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, activityId, config);
    }
}
