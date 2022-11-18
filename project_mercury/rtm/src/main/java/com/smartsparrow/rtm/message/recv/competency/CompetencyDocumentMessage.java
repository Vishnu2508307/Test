package com.smartsparrow.rtm.message.recv.competency;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CompetencyDocumentMessage extends ReceivedMessage implements DocumentMessage {

    private UUID documentId;

    @Override
    public UUID getDocumentId() {
        return documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentMessage that = (CompetencyDocumentMessage) o;
        return Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentMessage{" +
                "documentId=" + documentId +
                '}';
    }
}
