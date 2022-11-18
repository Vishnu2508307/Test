package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ProjectActivity {

    private UUID projectId;
    private UUID activityId;

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectActivity setProjectId(final UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public ProjectActivity setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectActivity that = (ProjectActivity) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, activityId);
    }

    @Override
    public String toString() {
        return "ProjectActivity{" +
                "projectId=" + projectId +
                ", activityId=" + activityId +
                '}';
    }
}
