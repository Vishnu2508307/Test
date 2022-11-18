package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeleteInteractiveMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private UUID parentPathwayId;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return interactiveId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DeleteInteractiveMessage that = (DeleteInteractiveMessage) o;
        return Objects.equal(getInteractiveId(), that.getInteractiveId()) && Objects
                .equal(parentPathwayId, that.parentPathwayId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getInteractiveId(), parentPathwayId);
    }

    @Override
    public String toString() {
        return "DeleteInteractiveMessage{" + "interactiveId=" + interactiveId + ", parentPathwayId=" + parentPathwayId
                + "} " + super.toString();
    }
}
