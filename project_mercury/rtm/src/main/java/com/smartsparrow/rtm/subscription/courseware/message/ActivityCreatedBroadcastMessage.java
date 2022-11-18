package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.Objects;
import java.util.UUID;

public class ActivityCreatedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -8652927786118044934L;
    private final UUID parentPathwayId;

    public ActivityCreatedBroadcastMessage(final UUID rootElementId,
                                           final UUID activityId,
                                           final UUID parentPathwayId) {
        super(rootElementId, activityId, ACTIVITY);
        this.parentPathwayId = parentPathwayId;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ActivityCreatedBroadcastMessage that = (ActivityCreatedBroadcastMessage) o;
        return Objects.equals(parentPathwayId, that.parentPathwayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentPathwayId);
    }

    @Override
    public String toString() {
        return "ActivityCreatedBroadcastMessage{" +
                "parentPathwayId=" + parentPathwayId +
                "} " + super.toString();
    }
}
