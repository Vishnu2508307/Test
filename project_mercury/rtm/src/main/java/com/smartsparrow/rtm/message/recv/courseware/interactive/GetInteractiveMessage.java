package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetInteractiveMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;

    public UUID getInteractiveId() {
        return interactiveId;
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
        GetInteractiveMessage that = (GetInteractiveMessage) o;
        return Objects.equal(getInteractiveId(), that.getInteractiveId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getInteractiveId());
    }

    @Override
    public String toString() {
        return "GetInteractiveMessage{" + "interactiveId=" + interactiveId + "} " + super.toString();
    }
}
