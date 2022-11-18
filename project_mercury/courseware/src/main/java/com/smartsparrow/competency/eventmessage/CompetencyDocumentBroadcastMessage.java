package com.smartsparrow.competency.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.dataevent.BroadcastMessage;

public class CompetencyDocumentBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = 6742519178389493358L;

    /**
     * Used only on {@link CompetencyDocumentAction#DOCUMENT_ITEM_DELETED}
     */
    private UUID documentItemId;

    /**
     * Always used
     */
    private UUID documentId;

    /**
     * This is only used on {@link CompetencyDocumentAction#ASSOCIATION_CREATED} and
     * {@link CompetencyDocumentAction#ASSOCIATION_DELETED}
     */
    private UUID associationId;

    /**
     * This is only used on {@link CompetencyDocumentAction#ASSOCIATION_CREATED} and
     * {@link CompetencyDocumentAction#ASSOCIATION_DELETED}
     */
    private AssociationType associationType;

    private CompetencyDocumentAction action;

    public UUID getDocumentItemId() {
        return documentItemId;
    }

    public CompetencyDocumentBroadcastMessage setDocumentItemId(UUID documentItemId) {
        this.documentItemId = documentItemId;
        return this;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public CompetencyDocumentBroadcastMessage setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public UUID getAssociationId() {
        return associationId;
    }

    public CompetencyDocumentBroadcastMessage setAssociationId(UUID associationId) {
        this.associationId = associationId;
        return this;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public CompetencyDocumentBroadcastMessage setAssociationType(AssociationType associationType) {
        this.associationType = associationType;
        return this;
    }

    public CompetencyDocumentAction getAction() {
        return action;
    }

    public CompetencyDocumentBroadcastMessage setAction(CompetencyDocumentAction action) {
        this.action = action;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentBroadcastMessage that = (CompetencyDocumentBroadcastMessage) o;
        return Objects.equals(documentItemId, that.documentItemId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(associationId, that.associationId) &&
                associationType == that.associationType &&
                action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemId, documentId, associationId, associationType, action);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentBroadcastMessage{" +
                "documentItemId=" + documentItemId +
                ", documentId=" + documentId +
                ", associationId=" + associationId +
                ", associationType=" + associationType +
                ", action=" + action +
                '}';
    }
}
