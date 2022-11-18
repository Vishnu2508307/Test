package com.smartsparrow.learner.service;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerDocumentGateway;
import com.smartsparrow.learner.data.LearnerDocumentItem;
import com.smartsparrow.learner.data.LearnerDocumentItemAssociationGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemLinkGateway;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerCompetencyDocumentService {

    private final LearnerDocumentGateway learnerDocumentGateway;
    private final LearnerDocumentItemGateway learnerDocumentItemGateway;
    private final LearnerDocumentItemLinkGateway learnerDocumentItemLinkGateway;
    private final LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway;

    @Inject
    public LearnerCompetencyDocumentService(LearnerDocumentGateway learnerDocumentGateway,
                                            LearnerDocumentItemGateway learnerDocumentItemGateway,
                                            LearnerDocumentItemLinkGateway learnerDocumentItemLinkGateway,
                                            LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway) {
        this.learnerDocumentGateway = learnerDocumentGateway;
        this.learnerDocumentItemGateway = learnerDocumentItemGateway;
        this.learnerDocumentItemLinkGateway = learnerDocumentItemLinkGateway;
        this.learnerDocumentItemAssociationGateway = learnerDocumentItemAssociationGateway;
    }

    /**
     * Find a published document by its id
     *
     * @param documentId the id of the document to find
     * @return a mono of learner document
     */
    @Trace(async = true)
    public Mono<LearnerDocument> findDocument(@Nonnull final UUID documentId) {
        return learnerDocumentGateway.find(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a published document item by id
     *
     * @param documentItemId the document item id
     * @return a mono of the learner document item
     */
    public Mono<LearnerDocumentItem> findItem(final UUID documentItemId) {
        return learnerDocumentItemGateway.findById(documentItemId);
    }

    /**
     * Find all the published document items that are linked to a learner walkable
     *
     * @param learnerWalkable the walkable to find the linked document items for
     * @return a flux of learner document item
     */
    @Trace(async = true)
    public Flux<LearnerDocumentItem> findLinkedItems(@Nonnull final LearnerWalkable learnerWalkable) {
        return learnerDocumentItemLinkGateway.find(learnerWalkable)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(learnerDocumentItemGateway::findById);
    }

    /**
     * Find all the published document items for a published document id
     *
     * @param documentId the id of the published document to find the published document items for
     * @return a flux of learner document item
     */
    @Trace(async = true)
    public Flux<LearnerDocumentItem> findItems(@Nonnull final UUID documentId) {
        return learnerDocumentItemGateway.findItems(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all published association to a document item of specific association type
     *
     * @param itemId the published document item destination id
     * @param associationType the association type
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findAssociationsTo(@Nonnull final UUID itemId,
                                                    final AssociationType associationType) {
        return learnerDocumentItemAssociationGateway.associationTo(itemId, associationType)
                .doOnEach(ReactiveTransaction.linkOnNext());

    }
    /**
     * Find all published association to a document item
     *
     * @param itemId the published document item destination id
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findAssociationsTo(@Nonnull final UUID itemId) {
        return learnerDocumentItemAssociationGateway.associationTo(itemId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Find all published associations from a document item of specific association type
     *
     * @param itemId the published document item origin id
     * @param associationType the association type
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findAssociationsFrom(@Nonnull final UUID itemId,
                                                      final AssociationType associationType) {
        return learnerDocumentItemAssociationGateway.associationFrom(itemId, associationType)
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Find all published associations from a document item
     *
     * @param itemId the published document item origin id
     * @return a flux of item associations
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findAssociationsFrom(@Nonnull final UUID itemId) {
        return learnerDocumentItemAssociationGateway.associationFrom(itemId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a published document item association by its id
     *
     * @param associationId the id of the association to find
     * @return a mono of item association
     */
    public Mono<ItemAssociation> findAssociation(@Nonnull final UUID associationId) {
        return learnerDocumentItemAssociationGateway.findById(associationId);
    }

    /**
     * Find all the associations for a published document
     *
     * @param documentId the published document id to find the associations for
     * @return a flux of published item associations
     */
    public Flux<ItemAssociation> findAssociations(@Nonnull final UUID documentId) {
        return learnerDocumentItemAssociationGateway.findAssociationIds(documentId)
                .flatMap(this::findAssociation);
    }
}
