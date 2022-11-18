package com.smartsparrow.learner.service;

import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B;
import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.buildDocument;
import static com.smartsparrow.competency.DocumentDataStubs.buildTag;
import static com.smartsparrow.courseware.CoursewareDataStubs.ELEMENT_ID;
import static com.smartsparrow.courseware.CoursewareDataStubs.mockLearnerWalkable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentVersion;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerDocumentGateway;
import com.smartsparrow.learner.data.LearnerDocumentItem;
import com.smartsparrow.learner.data.LearnerDocumentItemAssociationGateway;
import com.smartsparrow.learner.data.LearnerDocumentItemGateway;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.lang.PublishDocumentFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PublishCompetencyDocumentServiceTest {

    @InjectMocks
    private PublishCompetencyDocumentService publishCompetencyDocumentService;

    @Mock
    private LearnerDocumentGateway learnerDocumentGateway;

    @Mock
    private LearnerDocumentItemGateway learnerDocumentItemGateway;

    @Mock
    private LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway;

    @Mock
    private DocumentItemLinkService documentItemLinkService;

    @Mock
    private ItemAssociationService itemAssociationService;

    @Mock
    private DocumentService documentService;

    @Mock
    private DocumentItemService documentItemService;

    private static final UUID latestVersion = UUID.randomUUID();

    private LearnerWalkable learnerWalkable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        learnerWalkable = mockLearnerWalkable(CoursewareElementType.INTERACTIVE);

        when(learnerDocumentGateway.persist(any(LearnerDocument.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerDocumentItemAssociationGateway.persist(any(ItemAssociation.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerDocumentItemGateway.persist(any(LearnerDocumentItem.class))).thenReturn(Flux.just(new Void[]{}));

        when(documentItemLinkService.findAll(ELEMENT_ID)).thenReturn(Flux.just(
                buildTag(ELEMENT_ID, CoursewareElementType.INTERACTIVE, DOCUMENT_ID, ITEM_A_ID)
        ));

        when(documentService.findLatestVersion(DOCUMENT_ID)).thenReturn(Mono.just(
                new DocumentVersion()
                .setVersionId(latestVersion)
        ));

        when(documentService.fetchDocument(DOCUMENT_ID)).thenReturn(Mono.just(buildDocument()));
        when(itemAssociationService.findAllAssociations(ITEM_A_ID)).thenReturn(Flux.just(ASSOCIATION_A_B));
        when(documentItemService.findByDocumentId(DOCUMENT_ID)).thenReturn(Flux.just(
                ITEM_A
        ));

    }

    @Test
    void publishDocumentsFor_documentNotFound() {

        TestPublisher<Document> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException("not found"));

        when(documentService.fetchDocument(DOCUMENT_ID)).thenReturn(publisher.mono());

        assertThrows(PublishDocumentFault.class,
                () -> publishCompetencyDocumentService.publishDocumentsFor(learnerWalkable)
                        .collectList()
                        .block());

        verify(learnerDocumentGateway, never()).persist(any(LearnerDocument.class));
        verify(learnerDocumentItemAssociationGateway, never()).persist(any(ItemAssociation.class));
        verify(learnerDocumentItemGateway, never()).persist(any(LearnerDocumentItem.class));
    }

    @Test
    void publishDocumentFor_associationsNotFound() {
        when(itemAssociationService.findAllAssociations(ITEM_A_ID)).thenReturn(Flux.empty());

        List<LearnerDocument> published = publishCompetencyDocumentService.publishDocumentsFor(learnerWalkable)
                .collectList()
                .block();

        assertNotNull(published);
        assertEquals(1, published.size());

        verify(learnerDocumentGateway).persist(any(LearnerDocument.class));
        verify(learnerDocumentItemAssociationGateway, never()).persist(any(ItemAssociation.class));
        verify(learnerDocumentItemGateway).persist(any(LearnerDocumentItem.class));
    }

    @Test
    void publishDocumentFor_documentVersionNotFound() {
        TestPublisher<DocumentVersion> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException("not found"));

        when(documentService.findLatestVersion(DOCUMENT_ID)).thenReturn(publisher.mono());

        assertThrows(PublishDocumentFault.class,
                () -> publishCompetencyDocumentService.publishDocumentsFor(learnerWalkable)
                        .collectList()
                        .block());

        verify(learnerDocumentGateway, never()).persist(any(LearnerDocument.class));
        verify(learnerDocumentItemAssociationGateway, never()).persist(any(ItemAssociation.class));
        verify(learnerDocumentItemGateway, never()).persist(any(LearnerDocumentItem.class));
    }

    @Test
    void publishDocumentFor_success() {
        List<LearnerDocument> published = publishCompetencyDocumentService.publishDocumentsFor(learnerWalkable)
                .collectList()
                .block();

        assertNotNull(published);
        assertEquals(1, published.size());

        verify(learnerDocumentGateway).persist(any(LearnerDocument.class));
        verify(learnerDocumentItemAssociationGateway).persist(any(ItemAssociation.class));
        verify(learnerDocumentItemGateway).persist(any(LearnerDocumentItem.class));
    }

}
