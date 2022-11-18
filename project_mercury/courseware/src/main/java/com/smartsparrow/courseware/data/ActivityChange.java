package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class ActivityChange {

    private UUID activityId;
    private UUID changeId;

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityChange setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public ActivityChange setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityChange that = (ActivityChange) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, changeId);
    }

    @Override
    public String
    toString() {
        return "ActivityChange{" +
                "activityId=" + activityId +
                ", changeId=" + changeId +
                '}';
    }
}
