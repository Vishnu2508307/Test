package com.smartsparrow.competency.payload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;

import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "The competency document item link payload")
public class DocumentItemLinkPayload {

    private List<DocumentItemReferencePayload> documentItems;
    private UUID elementId;
    private CoursewareElementType elementType;

    public static DocumentItemLinkPayload from(@Nonnull CoursewareElement coursewareElement,
                                               @Nonnull List<DocumentItemReferencePayload> documentItems) {
        DocumentItemLinkPayload payload = new DocumentItemLinkPayload();
        payload.elementId = coursewareElement.getElementId();
        payload.elementType = coursewareElement.getElementType();
        payload.documentItems = documentItems;
        return payload;
    }

    public List<DocumentItemReferencePayload> getDocumentItems() {
        return documentItems;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemLinkPayload payload = (DocumentItemLinkPayload) o;
        return Objects.equals(documentItems, payload.documentItems) &&
                Objects.equals(elementId, payload.elementId) &&
                elementType == payload.elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItems, elementId, elementType);
    }

    @Override
    public String toString() {
        return "DocumentItemLinkPayload{" +
                "documentItems=" + documentItems +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                '}';
    }
}
