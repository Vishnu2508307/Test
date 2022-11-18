package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import com.smartsparrow.util.monitoring.ReactiveTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ItemAssociationGateway {

    private final ItemAssociationByDestinationMaterializer itemAssociationByDestinationMaterializer;
    private final ItemAssociationByDestinationMutator itemAssociationByDestinationMutator;
    private final ItemAssociationByDocumentMaterializer itemAssociationByDocumentMaterializer;
    private final ItemAssociationByDocumentMutator itemAssociationByDocumentMutator;
    private final ItemAssociationByOriginMaterializer itemAssociationByOriginMaterializer;
    private final ItemAssociationByOriginMutator itemAssociationByOriginMutator;
    private final ItemAssociationMaterializer itemAssociationMaterializer;
    private final ItemAssociationMutator itemAssociationMutator;
    private final Session session;

    @Inject
    public ItemAssociationGateway(ItemAssociationByDestinationMaterializer itemAssociationByDestinationMaterializer,
                                  ItemAssociationByDestinationMutator itemAssociationByDestinationMutator,
                                  ItemAssociationByDocumentMaterializer itemAssociationByDocumentMaterializer,
                                  ItemAssociationByDocumentMutator itemAssociationByDocumentMutator,
                                  ItemAssociationByOriginMaterializer itemAssociationByOriginMaterializer,
                                  ItemAssociationByOriginMutator itemAssociationByOriginMutator,
                                  ItemAssociationMaterializer itemAssociationMaterializer,
                                  ItemAssociationMutator itemAssociationMutator,
                                  Session session) {
        this.itemAssociationByDestinationMaterializer = itemAssociationByDestinationMaterializer;
        this.itemAssociationByDestinationMutator = itemAssociationByDestinationMutator;
        this.itemAssociationByDocumentMaterializer = itemAssociationByDocumentMaterializer;
        this.itemAssociationByDocumentMutator = itemAssociationByDocumentMutator;
        this.itemAssociationByOriginMaterializer = itemAssociationByOriginMaterializer;
        this.itemAssociationByOriginMutator = itemAssociationByOriginMutator;
        this.itemAssociationMaterializer = itemAssociationMaterializer;
        this.itemAssociationMutator = itemAssociationMutator;
        this.session = session;
    }

    /**
     * Fetch all document associations by document
     *
     * @param documentId - the document id to find the associations for
     * @return {@link Flux<ItemAssociation>}
     */
    public Flux<ItemAssociation> findAssociationByDocument(UUID documentId) {
        return ResultSets
                .query(session,
                        itemAssociationByDocumentMaterializer
                                .fetchAssociations(documentId))
                .flatMapIterable(row -> row)
                .map(itemAssociationByDocumentMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Fetch all document associations by item id and association type (where this item is the origin)
     *
     * @param itemId          - the item id to fetch all destinations for
     * @param associationType - the type of association with the destination. {@link AssociationType}
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findDestinations(UUID itemId, AssociationType associationType) {
        return ResultSets
                .query(session,
                        itemAssociationByOriginMaterializer.fetchDestinations(itemId, associationType))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(itemAssociationByOriginMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Fetch all document associations by item id (where this item is the origin)
     *
     * @param itemId - the item id to fetch all destinations for
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findDestinations(UUID itemId) {
        return ResultSets
                .query(session,
                        itemAssociationByOriginMaterializer.fetchDestinations(itemId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(itemAssociationByOriginMaterializer::fromRow)
                .flatMap(this::findById);
    }


    /**
     * Fetch all document associations by item id and association type (where this item is the destination)
     *
     * @param itemId          - the item id to fetch all origins for
     * @param associationType - the type of associations with the origin. {@link AssociationType}
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findOrigins(UUID itemId, AssociationType associationType) {
        return ResultSets
                .query(session,
                        itemAssociationByDestinationMaterializer.fetchOrigins(itemId, associationType))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(itemAssociationByDestinationMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Fetch all document associations by item id and association type (where this item is the destination)
     *
     * @param itemId - the item id to fetch all origins for
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findOrigins(UUID itemId) {
        return ResultSets
                .query(session,
                        itemAssociationByDestinationMaterializer.fetchOrigins(itemId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(itemAssociationByDestinationMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Find association by id
     *
     * @param associationId the association id
     * @return Mono with ItemAssociation, empty mono if association with the id does not exist
     */
    public Mono<ItemAssociation> findById(UUID associationId) {
        return ResultSets.query(session, itemAssociationMaterializer.fetchById(associationId))
                .flatMapIterable(row -> row)
                .map(itemAssociationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist association between two items
     *
     * @param itemAssociation the association to persist
     */
    public Flux<Void> persist(ItemAssociation itemAssociation) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(itemAssociationMutator,
                        itemAssociationByOriginMutator,
                        itemAssociationByDestinationMutator,
                        itemAssociationByDocumentMutator), itemAssociation);
        return Mutators.execute(session, stmt);
    }

    /**
     * Delete association between two items
     *
     * @param itemAssociation the association to delete
     */
    public Flux<Void> delete(ItemAssociation itemAssociation) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(itemAssociationMutator,
                        itemAssociationByOriginMutator,
                        itemAssociationByDestinationMutator,
                        itemAssociationByDocumentMutator), itemAssociation);
        return Mutators.execute(session, stmt);
    }
}
