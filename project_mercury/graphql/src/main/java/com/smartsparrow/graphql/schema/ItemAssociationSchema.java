package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.learner.payload.LearnerDocumentItemAssociationPayload;
import com.smartsparrow.learner.payload.LearnerDocumentItemPayload;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;

@Singleton
public class ItemAssociationSchema {

    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    public ItemAssociationSchema(LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    @GraphQLQuery(name = "originDocumentItem", description = "the document item of the origin id")
    public CompletableFuture<LearnerDocumentItemPayload> getOriginDocumentItem(@GraphQLContext LearnerDocumentItemAssociationPayload itemAssociation) {
        return learnerCompetencyDocumentService.findItem(itemAssociation.getOriginItemId())
                .map(LearnerDocumentItemPayload::from)
                .toFuture();
    }

    @GraphQLQuery(name = "destinationDocumentItem", description = "the document item of the association")
    public CompletableFuture<LearnerDocumentItemPayload> getDestinationDocumentItem(@GraphQLContext LearnerDocumentItemAssociationPayload itemAssociation) {
        return learnerCompetencyDocumentService.findItem(itemAssociation.getDestinationItemId())
                .map(LearnerDocumentItemPayload::from)
                .toFuture();
    }
}
