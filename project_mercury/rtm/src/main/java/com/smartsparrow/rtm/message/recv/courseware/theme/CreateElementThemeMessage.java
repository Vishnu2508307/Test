package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateElementThemeMessage extends ReceivedMessage implements ThemeMessage, CoursewareElementMessage {

    private UUID themeId;
    private UUID elementId;
    private CoursewareElementType elementType;

    @Override
    public UUID getThemeId() {
        return themeId;
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
        CreateElementThemeMessage that = (CreateElementThemeMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, elementId, elementType);
    }

    @Override
    public String toString() {
        return "CreateElementThemeMessage{" +
                "themeId=" + themeId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
