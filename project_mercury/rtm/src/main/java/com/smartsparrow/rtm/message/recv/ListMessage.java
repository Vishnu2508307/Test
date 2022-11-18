package com.smartsparrow.rtm.message.recv;

import java.util.Objects;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListMessage extends ReceivedMessage {

    private Integer collaboratorLimit;

    public Integer getCollaboratorLimit() {
        return collaboratorLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListMessage that = (ListMessage) o;
        return Objects.equals(collaboratorLimit, that.collaboratorLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collaboratorLimit);
    }

    @Override
    public String toString() {
        return "ListMessage{" +
                "collaboratorLimit=" + collaboratorLimit +
                '}';
    }
}
