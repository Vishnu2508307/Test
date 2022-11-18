package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;

import com.smartsparrow.competency.payload.DocumentPayload;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for a competency document mutation")
public class CompetencyDocumentMutationPayload {

    private DocumentPayload document;

    @GraphQLQuery(description = "The competency document")
    public DocumentPayload getDocument() {
        return document;
    }

    public CompetencyDocumentMutationPayload setDocument(DocumentPayload document) {
        this.document = document;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentMutationPayload that = (CompetencyDocumentMutationPayload) o;
        return Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentMutationPayload{" +
                "document=" + document +
                '}';
    }
}
