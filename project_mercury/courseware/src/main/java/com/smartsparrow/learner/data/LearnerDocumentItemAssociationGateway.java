package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import com.smartsparrow.util.monitoring.ReactiveTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerDocumentItemAssociationGateway {

    private final Session session;
    private final LearnerDocumentItemAssociationByDestinationMaterializer byDestinationMaterializer;
    private final LearnerDocumentItemAssociationByDestinationMutator byDestinationMutator;
    private final LearnerDocumentItemAssociationByDocumentMaterializer byDocumentMaterializer;
    private final LearnerDocumentItemAssociationByDocumentMutator byDocumentMutator;
    private final LearnerDocumentItemAssociationByOriginMaterializer byOriginMaterializer;
    private final LearnerDocumentItemAssociationByOriginMutator byOriginMutator;
    private final LearnerDocumentItemAssociationMaterializer learnerDocumentItemAssociationMaterializer;
    private final LearnerDocumentItemAssociationMutator learnerDocumentItemAssociationMutator;

    @Inject
    public LearnerDocumentItemAssociationGateway(Session session,
                                                 LearnerDocumentItemAssociationByDestinationMaterializer byDestinationMaterializer,
                                                 LearnerDocumentItemAssociationByDestinationMutator byDestinationMutator,
                                                 LearnerDocumentItemAssociationByDocumentMaterializer byDocumentMaterializer,
                                                 LearnerDocumentItemAssociationByDocumentMutator byDocumentMutator,
                                                 LearnerDocumentItemAssociationByOriginMaterializer byOriginMaterializer,
                                                 LearnerDocumentItemAssociationByOriginMutator byOriginMutator,
                                                 LearnerDocumentItemAssociationMaterializer learnerDocumentItemAssociationMaterializer,
                                                 LearnerDocumentItemAssociationMutator learnerDocumentItemAssociationMutator) {
        this.session = session;
        this.byDestinationMaterializer = byDestinationMaterializer;
        this.byDestinationMutator = byDestinationMutator;
        this.byDocumentMaterializer = byDocumentMaterializer;
        this.byDocumentMutator = byDocumentMutator;
        this.byOriginMaterializer = byOriginMaterializer;
        this.byOriginMutator = byOriginMutator;
        this.learnerDocumentItemAssociationMaterializer = learnerDocumentItemAssociationMaterializer;
        this.learnerDocumentItemAssociationMutator = learnerDocumentItemAssociationMutator;
    }

    /**
     * Persist an association between to items for a published competency document
     *
     * @param itemAssociation object representing the association
     * @return a flux of void
     */
    public Flux<Void> persist(final ItemAssociation itemAssociation) {
        return Mutators.execute(session, Flux.just(
                learnerDocumentItemAssociationMutator.upsert(itemAssociation),
                byOriginMutator.upsert(itemAssociation),
                byDestinationMutator.upsert(itemAssociation),
                byDocumentMutator.upsert(itemAssociation)
        ));
    }

    /**
     * Find a published item association
     *
     * @param associationId the association id
     * @return mono with published association if it exists, otherwise return empty mono
     */
    public Mono<ItemAssociation> findById(final UUID associationId) {
        return ResultSets.query(session, learnerDocumentItemAssociationMaterializer.find(associationId))
                .flatMapIterable(row -> row)
                .map(learnerDocumentItemAssociationMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the published association to the provided item id
     *
     * @param itemId the destination published item id of the association
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> associationTo(@Nonnull final UUID itemId) {
        return ResultSets.query(session, byDestinationMaterializer.fetchOrigins(itemId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row->row)
                .map(byDestinationMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Find all the published association to the provided item id for a given association type
     *
     * @param itemId the destination published item id of the association
     * @param associationType the association type to look for
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> associationTo(@Nonnull final UUID itemId, @Nonnull final AssociationType associationType) {
        return ResultSets.query(session, byDestinationMaterializer.fetchOrigins(itemId, associationType))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row->row)
                .map(byDestinationMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Find all the published associations from the provided item id
     *
     * @param itemId the origin item id
     * @return a flux of uuids representing the destination ids
     */
    @Trace(async = true)
    public Flux<ItemAssociation> associationFrom(@Nonnull final UUID itemId) {
        return ResultSets.query(session, byOriginMaterializer.fetchDestinations(itemId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row->row)
                .map(byOriginMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Find all the published associations from the provided item id for a given association type
     *
     * @param itemId the origin item id
     * @param associationType the type of association to look for
     * @return a flux of item associations
     */
    public Flux<ItemAssociation> associationFrom(@Nonnull final UUID itemId, @Nonnull final AssociationType associationType) {
        return ResultSets.query(session, byOriginMaterializer.fetchDestinations(itemId, associationType))
                .flatMapIterable(row->row)
                .map(byOriginMaterializer::fromRow)
                .flatMap(this::findById);
    }

    /**
     * Find all association ids for a published document
     *
     * @param documentId the published document to find all associations for
     * @return a flux of uuids representing the association ids
     */
    public Flux<UUID> findAssociationIds(@Nonnull final UUID documentId) {
        return ResultSets.query(session, byDocumentMaterializer.fetchAssociations(documentId))
                .flatMapIterable(row -> row)
                .map(byDocumentMaterializer::fromRow);
    }
}
