package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ActivityTheme {

    private UUID id;
    private UUID activityId;
    private String config;

    public UUID getId() {
        return id;
    }

    public ActivityTheme setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityTheme setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ActivityTheme setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityTheme that = (ActivityTheme) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, activityId, config);
    }

    @Override
    public String toString() {
        return "ActivityTheme{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", config='" + config + '\'' +
                '}';
    }
}
