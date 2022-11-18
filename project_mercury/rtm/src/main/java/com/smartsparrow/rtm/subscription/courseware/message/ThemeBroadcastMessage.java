package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ThemeBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -3312840587191999774L;
    private final UUID themeId;

    public ThemeBroadcastMessage(final UUID activityId,
                                 final UUID elementId,
                                 final CoursewareElementType type,
                                 final UUID themeId) {
        super(activityId, elementId, type);
        this.themeId = themeId;
    }

    public UUID getThemeId() {
        return themeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ThemeBroadcastMessage that = (ThemeBroadcastMessage) o;
        return Objects.equals(themeId, that.themeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), themeId);
    }

    @Override
    public String toString() {
        return "ThemeBroadcastMessage{" +
                "themeId=" + themeId +
                "} " + super.toString();
    }
}
