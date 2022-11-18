package com.smartsparrow.rtm.message.recv.learner.theme;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetSelectedThemeMessage extends ReceivedMessage {

    private UUID elementId;

    public UUID getElementId() {
        return elementId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetSelectedThemeMessage that = (GetSelectedThemeMessage) o;
        return Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId);
    }

    @Override
    public String toString() {
        return "GetSelectedThemeMessage{" +
                "elementId=" + elementId +
                '}';
    }
}
