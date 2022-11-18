package com.smartsparrow.rtm.message.recv.competency;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListCompetencyDocumentCollaboratorMessage extends ReceivedMessage implements DocumentMessage {

    private UUID documentId;
    private Integer limit;

    @Override
    public UUID getDocumentId() {
        return documentId;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListCompetencyDocumentCollaboratorMessage that = (ListCompetencyDocumentCollaboratorMessage) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, limit);
    }

    @Override
    public String toString() {
        return "ListCompetencyDocumentMessage{" +
                "documentId=" + documentId +
                ", limit=" + limit +
                '}';
    }
}
