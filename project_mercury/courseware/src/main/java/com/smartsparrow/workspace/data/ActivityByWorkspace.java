package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ActivityByWorkspace {

    private UUID activityId;
    private UUID workspaceId;

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityByWorkspace setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public ActivityByWorkspace setWorkspaceId(UUID workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityByWorkspace that = (ActivityByWorkspace) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(activityId, workspaceId);
    }
}
