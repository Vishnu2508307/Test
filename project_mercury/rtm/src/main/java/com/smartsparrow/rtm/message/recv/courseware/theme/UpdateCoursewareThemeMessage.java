package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdateCoursewareThemeMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;
    private String name;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateCoursewareThemeMessage that = (UpdateCoursewareThemeMessage) o;
        return Objects.equals(themeId, that.themeId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId, name);
    }

    @Override
    public String toString() {
        return "UpdateCoursewareThemeMessage{" +
                "themeId=" + themeId +
                ", name='" + name + '\'' +
                '}';
    }
}
