package com.smartsparrow.rtm.message.recv.courseware.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GenericThemeMessage extends ReceivedMessage implements ThemeMessage {

    private UUID themeId;

    @Override
    public UUID getThemeId() {
        return themeId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericThemeMessage that = (GenericThemeMessage) o;
        return Objects.equals(themeId, that.themeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(themeId);
    }

    @Override
    public String toString() {
        return "GenericThemeMessage{" +
                "themeId=" + themeId +
                '}';
    }
}
