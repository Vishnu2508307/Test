package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dataevent.BroadcastMessage;

public class ActivityBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -5869216431117763401L;

    private final UUID activityId;
    private final UUID elementId;
    private final CoursewareElementType elementType;

    public ActivityBroadcastMessage(UUID activityId, UUID elementId, CoursewareElementType elementType) {
        this.activityId = activityId;
        this.elementId = elementId;
        this.elementType = elementType;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityBroadcastMessage that = (ActivityBroadcastMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "ActivityBroadcastMessage{" +
                "activityId=" + activityId +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
