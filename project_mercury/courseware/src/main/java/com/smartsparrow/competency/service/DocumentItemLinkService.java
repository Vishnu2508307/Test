package com.smartsparrow.competency.service;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.DocumentItemLinkGateway;
import com.smartsparrow.competency.data.DocumentItemReference;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentItemLinkService {

    private final DocumentItemLinkGateway documentItemLinkGateway;

    @Inject
    public DocumentItemLinkService(DocumentItemLinkGateway documentItemLinkGateway) {
        this.documentItemLinkGateway = documentItemLinkGateway;
    }

    /**
     * Link a courseware element to a list of document items
     *
     * @param element the courseware element to be linked
     * @param documentItems a list of document items to link to the courseware element
     * @return a flux of object representing the link relationship
     */
    public Flux<DocumentItemTag> link(final @Nonnull CoursewareElement element,
                                      final @Nonnull List<DocumentItemReference> documentItems) {
        return Flux.just(documentItems.toArray(new DocumentItemReference[0]))
                .flatMap(documentItemReference ->
                        create(element.getElementId(),
                                element.getElementType(),
                                documentItemReference.getDocumentItemId(),
                                documentItemReference.getDocumentId()));
    }

    /**
     * Create and persist DocumentItemTag - link between courseware element and competency document item
     *
     * @param elementId      the courseware element id
     * @param elementType    the courseware element type
     * @param documentItemId the competency item id
     * @param documentId     the competency document id
     * @return Mono with created DocumentItemTag
     */
    @Trace(async = true)
    private Mono<DocumentItemTag> create(final UUID elementId,
                                         final CoursewareElementType elementType,
                                         final UUID documentItemId,
                                         final UUID documentId) {
        DocumentItemTag itemTag = new DocumentItemTag()
                .setElementType(elementType)
                .setElementId(elementId)
                .setDocumentItemId(documentItemId)
                .setDocumentId(documentId);

        return documentItemLinkGateway.persist(itemTag)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(itemTag));
    }

    /**
     * Unlink a courseware element to a list of document items
     *
     * @param element the courseware element to unlink
     * @param documentItems a list of document items to unlink from the courseware element
     * @return a flux of object representing the unlinked relationship
     */
    public Flux<DocumentItemTag> unlink(final @Nonnull CoursewareElement element,
                                        final @Nonnull List<DocumentItemReference> documentItems) {
        return Flux.just(documentItems.toArray(new DocumentItemReference[0]))
                .flatMap(documentItemReference -> {

                    DocumentItemTag itemTag = new DocumentItemTag()
                            .setElementType(element.getElementType())
                            .setElementId(element.getElementId())
                            .setDocumentItemId(documentItemReference.getDocumentItemId())
                            .setDocumentId(documentItemReference.getDocumentId());

                    return documentItemLinkGateway.delete(itemTag)
                            .thenMany(Flux.just(itemTag));
                });
    }

    /**
     * Find all the document items linked to the courseware element
     *
     * @param elementId the courseware element id to find the linked document items for
     * @return a flux of document item tag
     */
    @Trace(async = true)
    public Flux<DocumentItemTag> findAll(final UUID elementId) {
        return documentItemLinkGateway.fetchByElement(elementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find and copy all links from one element to another element
     *
     * @param elementId    the element which links should be copied
     * @param newElementId the new element where copy links to
     * @return Flux with new created copies
     */
    @Trace(async = true)
    public Flux<DocumentItemTag> duplicateLinks(final UUID elementId, final UUID newElementId) {
        return findAll(elementId)
                .flatMap(itemTag -> create(newElementId, itemTag.getElementType(), itemTag.getDocumentItemId(), itemTag.getDocumentId()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the existing links between a document item and courseware elements
     *
     * @param documentItemId the document item to find the links for
     * @return a flux of document item tags
     */
    public Flux<DocumentItemTag> findLinksByDocumentItem(final UUID documentItemId) {
        return documentItemLinkGateway.fetchByDocumentItem(documentItemId);
    }
}
