package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;

import com.smartsparrow.competency.payload.DocumentItemLinkPayload;

import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.types.GraphQLType;

@GraphQLType(description = "Payload for link a document item to a courseware element mutation")
public class CompetencyDocumentLinkPayload {

    private DocumentItemLinkPayload documentLink;

    @GraphQLQuery(description = "The linked document item")
    public DocumentItemLinkPayload getDocumentLink() {
        return documentLink;
    }

    public CompetencyDocumentLinkPayload setDocumentLink(DocumentItemLinkPayload documentLink) {
        this.documentLink = documentLink;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyDocumentLinkPayload that = (CompetencyDocumentLinkPayload) o;
        return Objects.equals(documentLink, that.documentLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentLink);
    }

    @Override
    public String toString() {
        return "CompetencyDocumentLinkPayload{" +
                "documentLink=" + documentLink +
                '}';
    }
}
