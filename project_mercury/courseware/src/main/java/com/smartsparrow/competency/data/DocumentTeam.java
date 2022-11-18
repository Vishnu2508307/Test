package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

public class DocumentTeam {

    private UUID documentId;
    private UUID teamId;

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentTeam setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public DocumentTeam setTeamId(UUID teamId) {
        this.teamId = teamId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentTeam that = (DocumentTeam) o;
        return Objects.equals(documentId, that.documentId) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, teamId);
    }

    @Override
    public String toString() {
        return "DocumentTeam{" +
                "documentId=" + documentId +
                ", teamId=" + teamId +
                '}';
    }
}
