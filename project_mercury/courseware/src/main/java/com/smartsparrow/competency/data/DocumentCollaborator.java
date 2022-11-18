package com.smartsparrow.competency.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.iam.collaborator.Collaborator;
import com.smartsparrow.iam.service.PermissionLevel;

public class DocumentCollaborator implements Collaborator {

    private UUID documentId;
    private PermissionLevel permissionLevel;

    public UUID getDocumentId() {
        return documentId;
    }

    public DocumentCollaborator setDocumentId(UUID documentId) {
        this.documentId = documentId;
        return this;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public DocumentCollaborator setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentCollaborator that = (DocumentCollaborator) o;
        return Objects.equals(documentId, that.documentId) &&
                permissionLevel == that.permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, permissionLevel);
    }

    @Override
    public String toString() {
        return "DocumentCollaborator{" +
                "documentId=" + documentId +
                ", permissionLevel=" + permissionLevel +
                '}';
    }
}
