package com.smartsparrow.competency.payload;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.util.DateFormat;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency document")
public class DocumentPayload {

    private UUID documentId;
    private String title;
    private String createdAt;
    private String lastChangedAt;
    private UUID workspaceId;

    public static DocumentPayload from(@Nonnull Document document) {
        DocumentPayload payload = new DocumentPayload();
        payload.documentId = document.getId();
        payload.title = document.getTitle();
        payload.createdAt = document.getCreatedAt() == null ? null : DateFormat.asRFC1123(document.getCreatedAt());
        payload.lastChangedAt = document.getModifiedAt() == null ? null : DateFormat.asRFC1123(document.getModifiedAt());
        payload.workspaceId = document.getWorkspaceId();
        return payload;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastChangedAt() {
        return lastChangedAt;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }
}
