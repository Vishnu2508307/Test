package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.graphql.service.GraphQLPageFactory;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentAssociationSchema {

    private final ItemAssociationService itemAssociationService;

    @Inject
    public DocumentAssociationSchema(ItemAssociationService itemAssociationService) {
        this.itemAssociationService = itemAssociationService;
    }

    /**
     * Get all associations for a document
     * @param document - the document id to get the associations for
     * @param itemAssociationId - the item association id to filter the search by
     * @param before - fetching only nodes before this node(exclusive)
     * @param last - fetching only the last certain number of nodes
     * @return
     */
    @GraphQLQuery(name = "associations", description = "list of associations for a document item")
    public CompletableFuture<Page<ItemAssociation>> getDocumentItemAssociations(@GraphQLContext Document document,
                                                                                @GraphQLArgument(name = "itemAssociationId",
                                                                     description = "Fetch the document item association with the specific id")
                                                                     UUID itemAssociationId,
                                                                                @GraphQLArgument(name = "before",
                                                                     description = "fetching only nodes before this node(exclusive)")
                                                                     String before,
                                                                                @GraphQLArgument(name = "last",
                                                                     description = "fetching only the last certain number of nodes")
                                                                     Integer last) {

        affirmArgument(document!=null, "document is required");

        if (itemAssociationId != null) {
            Mono<List<ItemAssociation>> associations = itemAssociationService
                    .findById(itemAssociationId)
                    .flux()
                    .collectList();
            return GraphQLPageFactory.createPage(associations, before, last).toFuture();
        }

        Mono<List<ItemAssociation>> associations = itemAssociationService
                .findAssociationByDocument(document.getId())
                .collectList();

        return GraphQLPageFactory.createPage(associations, before, last).toFuture();
    }
}
