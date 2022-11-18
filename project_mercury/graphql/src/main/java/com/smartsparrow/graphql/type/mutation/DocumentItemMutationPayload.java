package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;

import com.smartsparrow.competency.payload.DocumentItemPayload;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for a competency document item mutation")
public class DocumentItemMutationPayload {

    private DocumentItemPayload documentItem;

    @GraphQLQuery(description = "The document item")
    public DocumentItemPayload getDocumentItem() {
        return documentItem;
    }

    public DocumentItemMutationPayload setDocumentItem(DocumentItemPayload documentItem) {
        this.documentItem = documentItem;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemMutationPayload that = (DocumentItemMutationPayload) o;
        return Objects.equals(documentItem, that.documentItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItem);
    }

    @Override
    public String toString() {
        return "DocumentItemMutationPayload{" +
                "documentItem=" + documentItem +
                '}';
    }
}
