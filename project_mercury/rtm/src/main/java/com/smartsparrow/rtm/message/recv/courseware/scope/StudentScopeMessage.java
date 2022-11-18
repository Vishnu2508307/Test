package com.smartsparrow.rtm.message.recv.courseware.scope;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class StudentScopeMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private UUID studentScopeURN;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeMessage that = (StudentScopeMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(studentScopeURN, that.studentScopeURN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, studentScopeURN);
    }

    @Override
    public String toString() {
        return "StudentScopeMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", studentScopeURN=" + studentScopeURN +
                "} " + super.toString();
    }
}
