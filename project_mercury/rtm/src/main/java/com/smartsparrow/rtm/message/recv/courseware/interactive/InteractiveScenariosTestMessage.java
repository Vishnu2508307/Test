package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class InteractiveScenariosTestMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private String scopeData;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public String getScopeData() {
        return scopeData;
    }

    @Override
    public UUID getElementId() {
        return interactiveId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteractiveScenariosTestMessage that = (InteractiveScenariosTestMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(scopeData, that.scopeData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactiveId, scopeData);
    }

    @Override
    public String toString() {
        return "InteractiveScenariosTestMessage{" +
                "interactiveId=" + interactiveId +
                ", scope='" + scopeData + '\'' +
                "} " + super.toString();
    }
}
