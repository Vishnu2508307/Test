package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.payload.LearnerDocumentItemAssociationPayload;
import com.smartsparrow.learner.payload.LearnerDocumentItemPayload;
import com.smartsparrow.learner.payload.LearnerDocumentPayload;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentItemSchema {

    private final DocumentItemService documentItemService;
    private final ItemAssociationService itemAssociationService;
    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    public DocumentItemSchema(DocumentItemService documentItemService,
                              ItemAssociationService itemAssociationService,
                              LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        this.documentItemService = documentItemService;
        this.itemAssociationService = itemAssociationService;
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    /**
     * Find all the document items for a document
     *
     * @param document - the document object used as a context of
     * @param documentItemId - the id of the document item
     * @param before - fetch the nodes before this node
     * @param last - fetch only last n nodes
     * @return {@link Page<DocumentItem>} - the Page of document items
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.items")
    @GraphQLQuery(name = "items", description = "list of document items given a document")
    public CompletableFuture<Page<DocumentItem>> getDocumentItems(@GraphQLContext Document document,
                                                                  @GraphQLArgument(name = "documentItemId",
                                                                          description = "Fetch document item with specific id")
                                                                          UUID documentItemId,
                                                                  @GraphQLArgument(name = "before",
                                                                          description = "fetching only nodes before this node(exclusive)")
                                                                          String before,
                                                                  @GraphQLArgument(name = "last",
                                                                          description = "fetching only the last certain number of nodes")
                                                                          Integer last) {

        affirmArgument(document != null, "document is required");

        if (documentItemId != null) {
            //The documentItemId is not null so fetch only this documentItemId
            Mono<List<DocumentItem>> documentItemList = documentItemService
                    .findById(documentItemId)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .flux()
                    .collectList();
            return GraphQLPageFactory
                    .createPage(documentItemList, before, last)
                    .toFuture();
        }
        //else fetch all related documentItemIds
        Mono<List<DocumentItem>> documentItemList = documentItemService
                .findByDocumentId(document.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList();
        return GraphQLPageFactory
                .createPage(documentItemList, before, last)
                .toFuture();
    }

    /**
     * Find all associations where this document item is the origin
     *
     * @param documentItem - the documentItem id to find the associations for
     * @param associationType - the associationType to filter with
     * @param before - fetch the nodes before this node
     * @param last - fetch only last n nodes
     * @return {@link Page<ItemAssociation>} - the Page of document item associations
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.associationsFromOriginAssociations")
    @GraphQLQuery(name = "associationsFrom", description = "fetch all item associations where this item is the origin")
    public CompletableFuture<Page<ItemAssociation>> getOriginAssociations(@GraphQLContext DocumentItem documentItem,
                                                                          @GraphQLArgument(name = "associationType",
                                                                                  description = "fetch association with sepcific association type")
                                                                                  AssociationType associationType,
                                                                          @GraphQLArgument(name = "before",
                                                                                  description = "fetching only nodes before this node(exclusive)")
                                                                                  String before,
                                                                          @GraphQLArgument(name = "last",
                                                                                  description = "fetching only the last certain number of nodes")
                                                                                  Integer last) {

        affirmArgument(documentItem != null, "documentItem is required");

        if (associationType != null) {
            Mono<List<ItemAssociation>> itemAssociationList = itemAssociationService
                    .findOrigins(documentItem.getId(), associationType)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList();

            return GraphQLPageFactory
                    .createPage(itemAssociationList, before, last)
                    .toFuture();
        } else {
            Mono<List<ItemAssociation>> itemAssociationList = itemAssociationService
                    .findOrigins(documentItem.getId())
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList();

            return GraphQLPageFactory
                    .createPage(itemAssociationList, before, last)
                    .toFuture();
        }
    }

    /**
     * Find all associations where this document item is the destination
     *
     * @param documentItem - the documentItem id to find the associations for
     * @param associationType - the associationType to filte with
     * @param before - fetch the nodes before this node
     * @param last - fetch only last n nodes
     * @return {@link Page<ItemAssociation>} - the Page of document item associations
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.associationsToDestinationAssociations")
    @GraphQLQuery(name = "associationsTo", description = "fetch all item associations where this item is the destination")
    public CompletableFuture<Page<ItemAssociation>> getDestinationAssociations(@GraphQLContext DocumentItem documentItem,
                                                                               @GraphQLArgument(name = "associationType",
                                                                                       description = "fetch association with sepcific association type")
                                                                                       AssociationType associationType,
                                                                               @GraphQLArgument(name = "before",
                                                                                       description = "fetching only nodes before this node(exclusive)")
                                                                                       String before,
                                                                               @GraphQLArgument(name = "last",
                                                                                       description = "fetching only the last certain number of nodes")
                                                                                       Integer last) {

        affirmArgument(documentItem != null, "documentItem is required");

        if (associationType != null) {
            Mono<List<ItemAssociation>> itemAssociationList = itemAssociationService
                    .findDestinations(documentItem.getId(), associationType)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList();
            return GraphQLPageFactory
                    .createPage(itemAssociationList, before, last)
                    .toFuture();
        } else {
            Mono<List<ItemAssociation>> itemAssociationList = itemAssociationService
                    .findDestinations(documentItem.getId())
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList();
            return GraphQLPageFactory
                    .createPage(itemAssociationList, before, last)
                    .toFuture();
        }
    }

    /**
     * Find all the published document items that were linked to a learner walkable
     *
     * @param learnerWalkable the walkable to find the linked document items for
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of learner document item payload
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.linkedDocumentItems")
    @GraphQLQuery(name = "linkedDocumentItems", description = "fetch all document items that are linked to a learner walkable")
    public CompletableFuture<Page<LearnerDocumentItemPayload>> getLinkedItems(@GraphQLContext LearnerWalkable learnerWalkable,
                                                                              @GraphQLArgument(name = "before",
                                                                                      description = "fetching only nodes before this node(exclusive)")
                                                                                      String before,
                                                                              @GraphQLArgument(name = "last",
                                                                                      description = "fetching only the last certain number of nodes")
                                                                                      Integer last) {
        affirmArgument(learnerWalkable != null, "learnerWalkable is required");

        Mono<List<LearnerDocumentItemPayload>> linkedDocumentItems = learnerCompetencyDocumentService.findLinkedItems(
                        learnerWalkable)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .map(LearnerDocumentItemPayload::from)
                .collectList();

        return GraphQLPageFactory
                .createPage(linkedDocumentItems, before, last)
                .toFuture();
    }

    /**
     * Find all the published document items for a given published document
     *
     * @param learnerDocumentPayload the context where the query will be called
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of learner document items payload
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.documentItems")
    @GraphQLQuery(name = "documentItems", description = "fetch all published document items for a published document")
    public CompletableFuture<Page<LearnerDocumentItemPayload>> getDocumentItems(@GraphQLContext LearnerDocumentPayload learnerDocumentPayload,
                                                                                @GraphQLArgument(name = "before",
                                                                                        description = "fetching only nodes before this node(exclusive)")
                                                                                        String before,
                                                                                @GraphQLArgument(name = "last",
                                                                                        description = "fetching only the last certain number of nodes")
                                                                                        Integer last) {

        Mono<List<LearnerDocumentItemPayload>> learnerDocumentItems = learnerCompetencyDocumentService.findItems(
                        learnerDocumentPayload.getDocumentId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .map(LearnerDocumentItemPayload::from)
                .collectList();

        return GraphQLPageFactory
                .createPage(learnerDocumentItems, before, last)
                .toFuture();
    }

    /**
     * Find all published associations where the document item is the origin
     *
     * @param learnerDocumentItemPayload the published document item to find the associations for
     * @param associationType the association type to look for. When <code>null</code> all associations are returned
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of learner document item payload
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.associationsFromLearnerOriginAssociations")
    @GraphQLQuery(name = "associationsFrom", description = "fetch all learner item associations where this item is the origin")
    public CompletableFuture<Page<LearnerDocumentItemAssociationPayload>> getLearnerOriginAssociations(@GraphQLContext LearnerDocumentItemPayload learnerDocumentItemPayload,
                                                                                                       @GraphQLArgument(name = "associationType",
                                                                                                               description = "the type of association to fetch") @Nullable AssociationType associationType,
                                                                                                       @GraphQLArgument(name = "before",
                                                                                                               description = "fetching only nodes before this node(exclusive)")
                                                                                                               String before,
                                                                                                       @GraphQLArgument(name = "last",
                                                                                                               description = "fetching only the last certain number of nodes")
                                                                                                               Integer last) {

        Flux<ItemAssociation> itemAssociationFlux;
        if (associationType != null) {
            itemAssociationFlux = learnerCompetencyDocumentService
                    .findAssociationsFrom(learnerDocumentItemPayload.getId(), associationType);
        } else {
            itemAssociationFlux = learnerCompetencyDocumentService
                    .findAssociationsFrom(learnerDocumentItemPayload.getId());
        }

        Mono<List<LearnerDocumentItemAssociationPayload>> associations = itemAssociationFlux
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .map(LearnerDocumentItemAssociationPayload::from)
                .collectList();

        return GraphQLPageFactory
                .createPage(associations, before, last)
                .toFuture();
    }

    /**
     * Find all published associations where the document item is the destination
     *
     * @param learnerDocumentItemPayload the published document item to find the associations for
     * @param associationType the association type to look for. When <code>null</code> all associations are returned
     * @param before fetch the nodes before this node
     * @param last represents then number of the last nodes to fetch
     * @return a page of learner document item payload
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "DocumentItem.associationsToLearnerDestinationAssociations")
    @GraphQLQuery(name = "associationsTo", description = "fetch all published item associations where this item is the destination")
    public CompletableFuture<Page<LearnerDocumentItemAssociationPayload>> getLearnerDestinationAssociations(@GraphQLContext LearnerDocumentItemPayload learnerDocumentItemPayload,
                                                                                                            @GraphQLArgument(name = "associationType",
                                                                                                                    description = "the type of association to fetch") @Nullable AssociationType associationType,
                                                                                                            @GraphQLArgument(name = "before",
                                                                                                                    description = "fetching only nodes before this node(exclusive)")
                                                                                                                    String before,
                                                                                                            @GraphQLArgument(name = "last",
                                                                                                                    description = "fetching only the last certain number of nodes")
                                                                                                                    Integer last) {

        Flux<ItemAssociation> associationFlux;
        if (associationType != null) {
            associationFlux = learnerCompetencyDocumentService
                    .findAssociationsTo(learnerDocumentItemPayload.getId(), associationType);
        } else {
            associationFlux = learnerCompetencyDocumentService
                    .findAssociationsTo(learnerDocumentItemPayload.getId());
        }

        Mono<List<LearnerDocumentItemAssociationPayload>> associations = associationFlux
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .map(LearnerDocumentItemAssociationPayload::from)
                .collectList();

        return GraphQLPageFactory
                .createPage(associations, before, last)
                .toFuture();
    }
}
