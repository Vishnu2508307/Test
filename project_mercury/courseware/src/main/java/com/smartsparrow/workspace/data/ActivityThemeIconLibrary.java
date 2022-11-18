package com.smartsparrow.workspace.data;

import java.util.Objects;
import java.util.UUID;

public class ActivityThemeIconLibrary {

    private UUID activityId;
    private String iconLibrary;
    private IconLibraryState status;

    public UUID getActivityId() {
        return activityId;
    }

    public ActivityThemeIconLibrary setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getIconLibrary() {
        return iconLibrary;
    }

    public ActivityThemeIconLibrary setIconLibrary(final String iconLibrary) {
        this.iconLibrary = iconLibrary;
        return this;
    }

    public IconLibraryState getStatus() {
        return status;
    }

    public ActivityThemeIconLibrary setStatus(final IconLibraryState status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityThemeIconLibrary that = (ActivityThemeIconLibrary) o;
        return Objects.equals(activityId, that.activityId) && Objects.equals(iconLibrary,
                                                                             that.iconLibrary) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, iconLibrary, status);
    }

    @Override
    public String toString() {
        return "ActivityThemeIconLibrary{" +
                "activityId=" + activityId +
                ", iconLibrary='" + iconLibrary + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
