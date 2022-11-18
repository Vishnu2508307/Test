package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;

import com.smartsparrow.competency.payload.DocumentPayload;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for creating competency document mutation")
public class CompetencyDocumentCreatePayload {

    private DocumentPayload document;

    @GraphQLQuery(description = "The created document")
    public DocumentPayload getDocument() {
        return document;
    }

    public CompetencyDocumentCreatePayload setDocument(DocumentPayload document) {
        this.document = document;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentCreatePayload that = (CompetencyDocumentCreatePayload) o;
        return Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document);
    }
}
