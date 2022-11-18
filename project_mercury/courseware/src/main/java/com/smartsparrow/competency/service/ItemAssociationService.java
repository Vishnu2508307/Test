package com.smartsparrow.competency.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.data.ItemAssociationGateway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.data.LearnerDocumentItemAssociationGateway;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ItemAssociationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentItemService.class);

    private final ItemAssociationGateway itemAssociationGateway;
    private final DocumentService documentService;
    private final LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway;

    @Inject
    public ItemAssociationService(final ItemAssociationGateway itemAssociationGateway,
                                  final DocumentService documentService,
                                  final LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway) {
        this.itemAssociationGateway = itemAssociationGateway;
        this.documentService = documentService;
        this.learnerDocumentItemAssociationGateway = learnerDocumentItemAssociationGateway;
    }

    /**
     * Find all associations for a document
     *
     * @param documentId - the documentId to find the associations for
     * @return {@link Flux<ItemAssociation>}
     */
    public Flux<ItemAssociation> findAssociationByDocument(final UUID documentId) {
        return itemAssociationGateway.findAssociationByDocument(documentId);
    }

    /**
     * Find all destinations for a given item and associationType
     *
     * @param itemId          - the item id to get the association for
     * @param associationType - associationType of the item
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findDestinations(final UUID itemId, final AssociationType associationType) {
        return itemAssociationGateway
                .findDestinations(itemId, associationType)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all destinations for a given item and associationType
     *
     * @param itemId - the item id to get the association for
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findDestinations(final UUID itemId) {
        return itemAssociationGateway
                .findDestinations(itemId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all origins for a given item and associationType
     *
     * @param itemId          - the item id to get the associations for
     * @param associationType - associationType of the item
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findOrigins(final UUID itemId, final AssociationType associationType) {
        return itemAssociationGateway
                .findOrigins(itemId, associationType)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all origins for a given item and associationType
     *
     * @param itemId - the item id to get the associations for
     * @return {@link Flux<ItemAssociation>}
     */
    @Trace(async = true)
    public Flux<ItemAssociation> findOrigins(final UUID itemId) {
        return itemAssociationGateway
                .findOrigins(itemId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create an association between two items
     *
     * @param documentId        the document id
     * @param originItemId      the origin item id
     * @param destinationItemId the destination item id
     * @param associationType   the association type
     * @param creatorId         the creator account
     * @return Mono with created association
     */
    public Mono<ItemAssociation> create(final UUID documentId, final UUID originItemId, final UUID destinationItemId,
                                        AssociationType associationType, UUID creatorId) {
        checkArgument(documentId != null, "missing documentId");
        checkArgument(originItemId != null, "missing originItemId");
        checkArgument(destinationItemId != null, "missing destinationItemId");
        checkArgument(associationType != null, "missing associationType");
        checkArgument(creatorId != null, "missing creatorId");

        UUID id = UUIDs.timeBased();
        ItemAssociation association = new ItemAssociation()
                .setId(id)
                .setDocumentId(documentId)
                .setOriginItemId(originItemId)
                .setDestinationItemId(destinationItemId)
                .setAssociationType(associationType)
                .setCreatedAt(id)
                .setCreatedById(creatorId);

        return itemAssociationGateway.persist(association)
                .thenMany(documentService.updateVersion(documentId, creatorId))
                .then(Mono.just(association));
    }

    /**
     * Delete an association. It allows to delete association only if it was not published yet.
     *
     * @param association the association to delete
     * @param accountId   account who is doing modification
     * @throws IllegalArgumentFault if some parameters are null, or association was published before and can not be deleted
     */
    public Mono<ItemAssociation> delete(final ItemAssociation association, final UUID accountId) {
        checkArgument(association != null, "missing association");
        checkArgument(accountId != null, "missing accountId");

        return isAssociationPublished(association.getId())
                .doOnNext(isPublished -> {
                    if (isPublished) throw new IllegalArgumentFault("Association can not be deleted");
                })
                .then(itemAssociationGateway.delete(association)
                        .thenMany(documentService.updateVersion(association.getDocumentId(), accountId))
                        .then(Mono.just(association)));
    }

    /**
     * Find association by id
     *
     * @param associationId the association id
     * @return Mono with found association
     * @throws NotFoundFault if association is not found for the given id
     */
    public Mono<ItemAssociation> findById(final UUID associationId) {
        checkArgument(associationId != null, "missing associationId");

        return itemAssociationGateway.findById(associationId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new NotFoundFault(String.format("Association %s does not exist", associationId));
                });
    }

    /**
     * Find all the possible associations a document item has or belong to
     *
     * @param documentItemId the document item to find all the associations for
     * @return a flux of item association
     */
    public Flux<ItemAssociation> findAllAssociations(final UUID documentItemId) {
        return Flux.merge(findDestinations(documentItemId), findOrigins(documentItemId));
    }

    /**
     * Check if association was published or not.
     * @param associationId the association id
     * @return Mono with <code>true</code> if the association was published, with <code>false</code> otherwise
     */
    public Mono<Boolean> isAssociationPublished(final UUID associationId) {
        return learnerDocumentItemAssociationGateway.findById(associationId).hasElement();
    }
}
