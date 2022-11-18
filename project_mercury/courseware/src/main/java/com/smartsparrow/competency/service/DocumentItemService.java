package com.smartsparrow.competency.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.DocumentItemGateway;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerDocumentItemGateway;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentItemService {

    private final DocumentItemGateway documentItemGateway;
    private final DocumentService documentService;
    private final ItemAssociationService itemAssociationService;
    private final LearnerDocumentItemGateway learnerDocumentItemGateway;
    private final DocumentItemLinkService documentItemLinkService;

    @Inject
    public DocumentItemService(final DocumentItemGateway documentItemGateway,
                               final DocumentService documentService,
                               final ItemAssociationService itemAssociationService,
                               final LearnerDocumentItemGateway learnerDocumentItemGateway,
                               final DocumentItemLinkService documentItemLinkService) {
        this.documentItemGateway = documentItemGateway;
        this.documentService = documentService;
        this.itemAssociationService = itemAssociationService;
        this.learnerDocumentItemGateway = learnerDocumentItemGateway;
        this.documentItemLinkService = documentItemLinkService;
    }

    /**
     * Find document items by document id
     *
     * @param documentId - the document id to find associated document items
     * @return {@link Flux<DocumentItem>}
     */
    @Trace(async = true)
    public Flux<DocumentItem> findByDocumentId(final UUID documentId) {
        return documentItemGateway.findByDocumentId(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /* Create a new document item.
     *
     * @param accountId the account id that is creating the document item
     * @param documentId the document id the item will belong to
     * @param provided the provided document item object; only the following fields will be read from this object:
     * <ul>
     *   <li>{@link DocumentItem#getAbbreviatedStatement()}</li>
     *   <li>{@link DocumentItem#getFullStatement()} ()}</li>
     *   <li>{@link DocumentItem#getHumanCodingScheme()} ()}</li>
     * </ul>
     * @return a mono of the created document item payload
     */
    public Mono<DocumentItemPayload> create(final UUID accountId, final UUID documentId, final DocumentItem provided) {
        // create the document item
        DocumentItem newDocumentItem = new DocumentItem()
                .setDocumentId(documentId)
                .setAbbreviatedStatement(provided.getAbbreviatedStatement())
                .setFullStatement(provided.getFullStatement())
                .setHumanCodingScheme(provided.getHumanCodingScheme())
                .setId(UUID.randomUUID())
                .setCreatedAt(UUIDs.timeBased())
                .setCreatedById(accountId);

        return documentItemGateway.persist(newDocumentItem)
                .thenMany(documentService.updateVersion(documentId, accountId))
                .singleOrEmpty()
                .then(Mono.just(newDocumentItem))
                .map(DocumentItemPayload::from);
    }

    /**
     * Update an existing document item. This method will update the old values with the new values
     *
     * @param accountId      the account id that is editing the document item
     * @param documentItemId the document item id
     * @param provided       the provided document item object; only the following fields will be read from this object:
     *                       <ul>
     *                       <li>{@link DocumentItem#getAbbreviatedStatement()}</li>
     *                       <li>{@link DocumentItem#getFullStatement()} ()}</li>
     *                       <li>{@link DocumentItem#getHumanCodingScheme()} ()}</li>
     *                       </ul>
     * @return a mono of the updated document item
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    public Mono<DocumentItem> update(final UUID accountId, final UUID documentId, final UUID documentItemId,
                                     final DocumentItem provided) {

        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(documentId != null, "documentId is required");
        affirmArgument(documentItemId != null, "documentItemId is required");
        affirmArgument(provided != null, "documentItem is required");

        DocumentItem updated = new DocumentItem()
                .setId(documentItemId)
                .setAbbreviatedStatement(provided.getAbbreviatedStatement())
                .setFullStatement(provided.getFullStatement())
                .setHumanCodingScheme(provided.getHumanCodingScheme())
                .setDocumentId(documentId)
                .setModifiedAt(UUIDs.timeBased())
                .setModifiedById(accountId);

        return documentItemGateway.update(updated)
                .thenMany(documentService.updateVersion(documentId, accountId))
                .singleOrEmpty()
                .thenReturn(updated);
    }

    /**
     * Delete a document item. It allows to delete items which were not published yet.
     *
     * @param accountId      the id of the account that is deleting the document item
     * @param documentId     the id of the document the document item belongs to
     * @param documentItemId the id of the document item to delete
     * @return a mono of the deleted document item
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    public Mono<DocumentItem> delete(final UUID accountId, final UUID documentId, final UUID documentItemId) {

        affirmArgument(accountId != null, "accountId is required");
        affirmArgument(documentId != null, "documentId is required");
        affirmArgument(documentItemId != null, "documentItemId is required");

        DocumentItem deleted = new DocumentItem()
                .setId(documentItemId)
                .setDocumentId(documentId)
                .setModifiedById(accountId)
                .setModifiedAt(UUIDs.timeBased());

        return isItemPublished(documentItemId)
                .doOnNext(isItemPublished -> {
                    if (isItemPublished) throw new IllegalArgumentFault("Published document item can not be deleted");
                })
                .then(isItemLinked(documentItemId)
                .doOnNext(isItemLinked -> {
                    if (isItemLinked) throw new IllegalArgumentFault("Linked document item can not be deleted");
                }))
                .then(documentItemGateway.delete(deleted)
                        .thenMany(documentService.updateVersion(documentId, accountId))
                        .thenMany(deleteAllAssociations(documentItemId, accountId))
                        .then(Mono.just(deleted)));

    }

    /**
     * Delete all existing associations with a document item
     *
     * @param itemId    the document item id to delete all the associations for
     * @param accountId the account id of the user deleting the associations
     * @return a flux of item association
     */
    Flux<ItemAssociation> deleteAllAssociations(final UUID itemId, final UUID accountId) {
        return Flux.merge(itemAssociationService.findDestinations(itemId), itemAssociationService.findOrigins(itemId))
                .flatMap(itemAssociation -> itemAssociationService.delete(itemAssociation, accountId));
    }

    /**
     * Fetch a document item payload by id
     *
     * @param id the id of the document item to find
     * @return a mono of document item payload object
     */
    public Mono<DocumentItemPayload> getDocumentItemPayload(UUID id) {
        return documentItemGateway.find(id)
                .map(DocumentItemPayload::from);
    }

    /**
     * Find a document item by id
     *
     * @param itemId the id of the document item to find
     * @return a mono of item, empty mono if item does not exist
     */
    @Trace(async = true)
    public Mono<DocumentItem> findById(final UUID itemId) {
        checkArgument(itemId != null, "missing itemId");

        return documentItemGateway.find(itemId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Check if document item was published or not.
     *
     * @param itemId the document item
     * @return Mono with <code>true</code> if the document item was published, with <code>false</code> otherwise
     */
    public Mono<Boolean> isItemPublished(final UUID itemId) {
        return learnerDocumentItemGateway.findById(itemId).hasElement();
    }

    /**
     * Check if a document item has been linked with one or more courseware elements
     *
     * @param itemId the document item to check the existing links for
     * @return a mono with <code>true</code> if the document item has been linked or <code>false</code> if otherwise
     */
    public Mono<Boolean> isItemLinked(final UUID itemId) {
        return documentItemLinkService.findLinksByDocumentItem(itemId).hasElements();
    }

    /**
     * Find all document items linked to a courseware element
     *
     * @param coursewareElementId the courseware element
     * @return flux of linked items
     */
    @Trace(async = true)
    public Flux<DocumentItem> findAllLinked(final UUID coursewareElementId) {
        checkArgument(coursewareElementId != null, "missing coursewareElementId");

        return documentItemLinkService.findAll(coursewareElementId)
                .flatMap(tag -> findById(tag.getDocumentItemId()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
