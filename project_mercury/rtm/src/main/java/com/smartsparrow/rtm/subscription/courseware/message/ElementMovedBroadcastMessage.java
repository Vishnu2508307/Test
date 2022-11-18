package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ElementMovedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -4108879088002155199L;
    private final UUID fromPathwayId;
    private final UUID toPathwayId;

    public ElementMovedBroadcastMessage(final UUID activityId,
                                        final UUID elementId,
                                        final CoursewareElementType type,
                                        final UUID fromPathwayId,
                                        final UUID toPathwayId) {
        super(activityId, elementId, type);
        this.fromPathwayId = fromPathwayId;
        this.toPathwayId = toPathwayId;
    }

    public UUID getFromPathwayId() {
        return fromPathwayId;
    }

    public UUID getToPathwayId() {
        return toPathwayId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ElementMovedBroadcastMessage that = (ElementMovedBroadcastMessage) o;
        return Objects.equals(fromPathwayId, that.fromPathwayId) &&
                Objects.equals(toPathwayId, that.toPathwayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fromPathwayId, toPathwayId);
    }

    @Override
    public String toString() {
        return "ElementMovedBroadcastMessage{" +
                "fromPathwayId=" + fromPathwayId +
                "toPathwayId=" + toPathwayId +
                "} " + super.toString();
    }
}
