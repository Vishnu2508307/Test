package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PublicationJobGenericMessage extends ReceivedMessage implements PublicationJobMessage {

    private UUID publicationId;

    @Override
    public UUID getPublicationId() {
        return publicationId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationJobGenericMessage that = (PublicationJobGenericMessage) o;
        return Objects.equals(publicationId, that.publicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId);
    }

    @Override
    public String toString() {
        return "PublicationJobGenericMessage{" +
                "publicationId=" + publicationId +
                "} " + super.toString();
    }
}
