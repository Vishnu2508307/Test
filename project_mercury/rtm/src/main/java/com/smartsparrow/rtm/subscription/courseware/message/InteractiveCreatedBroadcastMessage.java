package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.Objects;
import java.util.UUID;

public class InteractiveCreatedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 8061154095087780765L;
    private final UUID parentPathwayId;

    public InteractiveCreatedBroadcastMessage(UUID activityId, UUID interactiveId, UUID parentPathwayId) {
        super(activityId, interactiveId, INTERACTIVE);
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
        InteractiveCreatedBroadcastMessage that = (InteractiveCreatedBroadcastMessage) o;
        return Objects.equals(parentPathwayId, that.parentPathwayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentPathwayId);
    }

    @Override
    public String toString() {
        return "InteractiveCreatedBroadcastMessage{" +
                "parentPathwayId=" + parentPathwayId +
                "} " + super.toString();
    }
}
