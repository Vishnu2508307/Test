package com.smartsparrow.learner.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.competency.data.DocumentVersion;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerDocumentGateway;
import com.smartsparrow.learner.data.LearnerDocumentItem;
import com.smartsparrow.learner.data.LearnerDocumentItemAssociationGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemGateway;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.lang.PublishDocumentFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PublishCompetencyDocumentService {

    private static final Logger log = LoggerFactory.getLogger(PublishCompetencyDocumentService.class);

    private final LearnerDocumentGateway learnerDocumentGateway;
    private final LearnerDocumentItemGateway learnerDocumentItemGateway;
    private final LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway;
    private final DocumentItemLinkService documentItemLinkService;
    private final ItemAssociationService itemAssociationService;
    private final DocumentService documentService;
    private final DocumentItemService documentItemService;

    @Inject
    public PublishCompetencyDocumentService(LearnerDocumentGateway learnerDocumentGateway,
                                            LearnerDocumentItemGateway learnerDocumentItemGateway,
                                            LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway,
                                            DocumentItemLinkService documentItemLinkService,
                                            ItemAssociationService itemAssociationService,
                                            DocumentService documentService,
                                            DocumentItemService documentItemService) {
        this.learnerDocumentGateway = learnerDocumentGateway;
        this.learnerDocumentItemGateway = learnerDocumentItemGateway;
        this.learnerDocumentItemAssociationGateway = learnerDocumentItemAssociationGateway;
        this.documentItemLinkService = documentItemLinkService;
        this.itemAssociationService = itemAssociationService;
        this.documentService = documentService;
        this.documentItemService = documentItemService;
    }

    /**
     * Find all the document items linked to the walkable and for each one the document, the item and the associations
     * are published.
     *
     * @param learnerWalkable the learner walkable to publish the documents for
     * @return a flux of published learner documents
     * @throws PublishDocumentFault when failing to publish
     */
    public Flux<LearnerDocument> publishDocumentsFor(final LearnerWalkable learnerWalkable) {

        // find all document items linked to the element
        return documentItemLinkService.findAll(learnerWalkable.getId())
                // get distinct document id
                .distinct(DocumentItemTag::getDocumentId)
                // collect the document id
                .map(DocumentItemTag::getDocumentId)
                // for each distinct document id, publish the document
                .flatMap(this::publishDocument)
                // handle any error
                .doOnError(Throwable.class, ex -> {

                    if (!(ex instanceof PublishDocumentFault)) {
                        if (log.isDebugEnabled()) {
                            log.debug("error while publishing documents for {}{}", learnerWalkable.toString(), ex.getMessage());
                        }
                    }

                    throw new PublishDocumentFault("an unexpected error occurred");
                });
    }

    /**
     * Find all the possible associations for a document item id and publish each association
     *
     * @param documentItemId the document id to publish the existing associations for
     * @return a flux of item association
     */
    private Flux<ItemAssociation> publishAssociations(final UUID documentItemId) {
        return itemAssociationService.findAllAssociations(documentItemId)
                .flatMap(itemAssociation -> {
                    return learnerDocumentItemAssociationGateway.persist(itemAssociation)
                            .then(Mono.just(itemAssociation));
                });
    }

    /**
     * Publish the entire competency document, all document items are found and published along with all existing
     * association between each individual item in the document map. Then the document itself is published.
     *
     * @param documentId the id of the document to publish
     * @return a mono of learner document
     * @throws PublishDocumentFault when either the document or the document version are not found
     */
    private Mono<LearnerDocument> publishDocument(final UUID documentId) {

        Mono<DocumentVersion> versionMono = documentService.findLatestVersion(documentId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PublishDocumentFault(
                            String.format("version not found for document %s", documentId)
                    );
                });
        Mono<Document> documentMono = documentService.fetchDocument(documentId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PublishDocumentFault(
                            String.format("document with id %s not found", documentId)
                    );
                });

        // find all the document items
        final Flux<LearnerDocumentItem> publishDocumentItems = documentItemService.findByDocumentId(documentId)
                // publish each document item
                .flatMap(this::publishDocumentItem);

        return Mono.zip(documentMono, versionMono)
                .flatMap(tuple2 -> {
                    Document document = tuple2.getT1();
                    DocumentVersion documentVersion = tuple2.getT2();

                    LearnerDocument learnerDocument = new LearnerDocument()
                            .setId(document.getId())
                            .setTitle(document.getTitle())
                            .setCreatedAt(document.getCreatedAt())
                            .setCreatedBy(document.getCreatedBy())
                            .setModifiedAt(document.getModifiedAt())
                            .setModifiedBy(document.getModifiedBy())
                            .setOrigin(document.getOrigin())
                            .setDocumentVersionId(documentVersion.getVersionId());

                    return learnerDocumentGateway.persist(learnerDocument)
                            .thenMany(publishDocumentItems)
                            .then(Mono.just(learnerDocument));
                });
    }

    /**
     * Publish a document item and all the existing associations with it
     *
     * @param documentItem the document item to publish
     * @return a mono of a learner document item
     */
    private Mono<LearnerDocumentItem> publishDocumentItem(final DocumentItem documentItem) {
        LearnerDocumentItem learnerDocumentItem = new LearnerDocumentItem()
                .setId(documentItem.getId())
                .setFullStatement(documentItem.getFullStatement())
                .setAbbreviatedStatement(documentItem.getAbbreviatedStatement())
                .setHumanCodingScheme(documentItem.getHumanCodingScheme())
                .setCreatedAt(documentItem.getCreatedAt())
                .setCreatedBy(documentItem.getCreatedById())
                .setModifiedAt(documentItem.getModifiedAt())
                .setModifiedBy(documentItem.getModifiedById())
                .setDocumentId(documentItem.getDocumentId());

        return learnerDocumentItemGateway.persist(learnerDocumentItem)
                .thenMany(publishAssociations(documentItem.getId()))
                .then(Mono.just(learnerDocumentItem));
    }
}
