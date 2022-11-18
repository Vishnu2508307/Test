package com.smartsparrow.rtm.message.recv.courseware.scope;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListSourcesRegisteredToScopeMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID scopeURN;
    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getScopeURN() {
        return scopeURN;
    }

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListSourcesRegisteredToScopeMessage that = (ListSourcesRegisteredToScopeMessage) o;
        return Objects.equals(scopeURN, that.scopeURN) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeURN, elementId, elementType);
    }

    @Override
    public String toString() {
        return "ListSourcesRegisteredToScopeMessage{" +
                "scopeURN=" + scopeURN +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
