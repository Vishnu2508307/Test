package com.smartsparrow.competency.service;

import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B;
import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_B_A;
import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.DocumentItemGateway;
import com.smartsparrow.competency.data.DocumentItemTag;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerDocumentItem;
import com.smartsparrow.learner.data.LearnerDocumentItemGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DocumentItemServiceTest {

    @InjectMocks
    private DocumentItemService documentItemService;

    @Mock
    private DocumentItemGateway documentItemGateway;
    @Mock
    private DocumentService documentService;
    @Mock
    private ItemAssociationService itemAssociationService;
    @Mock
    private LearnerDocumentItemGateway learnerDocumentItemGateway;
    @Mock
    private DocumentItemLinkService documentItemLinkService;

    private TestPublisher<Void> versionUpdatePublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        versionUpdatePublisher = TestPublisher.<Void>create().complete();

        when(itemAssociationService.findOrigins(ITEM_A_ID)).thenReturn(Flux.just(ASSOCIATION_A_B));
        when(itemAssociationService.findDestinations(ITEM_A_ID)).thenReturn(Flux.just(ASSOCIATION_B_A));

        when(learnerDocumentItemGateway.findById(any())).thenReturn(Mono.empty());
        when(documentItemLinkService.findLinksByDocumentItem(any())).thenReturn(Flux.empty());
    }

    @Test
    void create() {

        ArgumentCaptor<DocumentItem> captor = ArgumentCaptor.forClass(DocumentItem.class);

        UUID accountId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentItem item = new DocumentItem()
                .setFullStatement("full statement")
                .setAbbreviatedStatement("fs")
                .setHumanCodingScheme("coding scheme");

        when(documentItemGateway.persist(any(DocumentItem.class))).thenReturn(Flux.just(new Void[]{}));
        when(documentService.updateVersion(documentId, accountId)).thenReturn(versionUpdatePublisher.flux());

        DocumentItemPayload created = documentItemService.create(accountId, documentId, item).block();

        assertNotNull(created);
        assertEquals(documentId, created.getDocumentId());
        assertEquals("full statement", created.getFullStatement());
        assertEquals("fs", created.getAbbreviatedStatement());
        assertEquals("coding scheme", created.getHumanCodingScheme());
        assertNotNull(created.getCreatedAt());
        assertNull(created.getModifiedAt());
        assertNotNull(created.getId());

        verify(documentItemGateway).persist(captor.capture());

        DocumentItem captured = captor.getValue();

        assertNotNull(captured);
        assertNotNull(captured.getId());
        assertEquals(documentId, captured.getDocumentId());
        assertEquals(accountId, captured.getCreatedById());
        assertNotNull(captured.getCreatedAt());
        assertNull(captured.getModifiedById());
        assertEquals(accountId, captured.getCreatedById());

        versionUpdatePublisher.assertWasRequested();
    }

    @Test
    void replace() {
        UUID accountId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID documentItemId = UUID.randomUUID();

        DocumentItem item = new DocumentItem()
                .setFullStatement("replaced statement")
                .setAbbreviatedStatement("rs")
                .setHumanCodingScheme("new coding scheme");

        when(documentItemGateway.update(any(DocumentItem.class))).thenReturn(Flux.just(new Void[]{}));
        when(documentService.updateVersion(documentId, accountId)).thenReturn(versionUpdatePublisher.flux());

        DocumentItem replaced = documentItemService.update(accountId, documentId, documentItemId, item).block();

        assertNotNull(replaced);
        assertEquals(documentId, replaced.getDocumentId());
        assertEquals("replaced statement", replaced.getFullStatement());
        assertEquals("rs", replaced.getAbbreviatedStatement());
        assertEquals("new coding scheme", replaced.getHumanCodingScheme());
        assertNull(replaced.getCreatedAt());
        assertNull(replaced.getCreatedById());
        assertEquals(accountId, replaced.getModifiedById());
        assertNotNull(replaced.getModifiedAt());
        assertEquals(documentItemId, replaced.getId());

        versionUpdatePublisher.assertWasRequested();
    }

    @Test
    @Disabled
    void delete() {
        UUID accountId = UUID.randomUUID();

        DocumentItemService spy = Mockito.spy(documentItemService);
        doReturn(Flux.just(ASSOCIATION_A_B, ASSOCIATION_B_A)).when(spy).deleteAllAssociations(ITEM_A_ID, accountId);

        ArgumentCaptor<DocumentItem> captor = ArgumentCaptor.forClass(DocumentItem.class);

        when(documentItemGateway.delete(any(DocumentItem.class))).thenReturn(Flux.just(new Void[]{}));
        when(documentService.updateVersion(DOCUMENT_A_ID, accountId)).thenReturn(versionUpdatePublisher.flux());

        DocumentItem deleted = documentItemService.delete(accountId, DOCUMENT_A_ID, ITEM_A_ID).block();

        assertNotNull(deleted);
        assertEquals(DOCUMENT_A_ID, deleted.getDocumentId());
        assertEquals(ITEM_A_ID, deleted.getId());

        verify(documentItemGateway).delete(captor.capture());

        DocumentItem captured = captor.getValue();

        assertNotNull(captured);
        assertEquals(ITEM_A_ID, captured.getId());
        assertEquals(DOCUMENT_A_ID, captured.getDocumentId());
        assertEquals(accountId, captured.getModifiedById());
        assertNotNull(captured.getModifiedAt());

        versionUpdatePublisher.assertWasRequested();
    }

    @Test
    void delete_publishedItem() {
        UUID accountId = UUID.randomUUID();
        when(learnerDocumentItemGateway.findById(eq(ITEM_A_ID))).thenReturn(Mono.just(new LearnerDocumentItem().setId(ITEM_A_ID)));
        TestPublisher<Void> deletePublisher = TestPublisher.<Void>create().complete();
        when(documentItemGateway.delete(any(DocumentItem.class))).thenReturn(deletePublisher.flux());
        when(documentService.updateVersion(DOCUMENT_A_ID, accountId)).thenReturn(versionUpdatePublisher.flux());

        assertThrows(IllegalArgumentFault.class, () -> documentItemService.delete(accountId, DOCUMENT_A_ID, ITEM_A_ID).block());

        versionUpdatePublisher.assertWasNotRequested();
        deletePublisher.assertWasNotRequested();
    }

    @Test
    void delete_linkedItem() {
        UUID accountId = UUID.randomUUID();
        when(documentItemLinkService.findLinksByDocumentItem(any())).thenReturn(Flux.just(new DocumentItemTag()));
        TestPublisher<Void> deletePublisher = TestPublisher.<Void>create().complete();
        when(documentItemGateway.delete(any(DocumentItem.class))).thenReturn(deletePublisher.flux());
        when(documentService.updateVersion(DOCUMENT_A_ID, accountId)).thenReturn(versionUpdatePublisher.flux());

        assertThrows(IllegalArgumentFault.class, () -> documentItemService.delete(accountId, DOCUMENT_A_ID, ITEM_A_ID).block());

        versionUpdatePublisher.assertWasNotRequested();
        deletePublisher.assertWasNotRequested();
    }

    @Test
    void deleteAllAssociations() {
        UUID accountId = UUID.randomUUID();

        when(itemAssociationService.delete(ASSOCIATION_A_B, accountId)).thenReturn(Mono.just(ASSOCIATION_A_B));
        when(itemAssociationService.delete(ASSOCIATION_B_A, accountId)).thenReturn(Mono.just(ASSOCIATION_B_A));

        List<ItemAssociation> deleted = documentItemService.deleteAllAssociations(ITEM_A_ID, accountId)
                .collectList()
                .block();

        assertNotNull(deleted);
        assertEquals(2, deleted.size());

        verify(itemAssociationService, times(2)).delete(any(ItemAssociation.class), eq(accountId));
    }

    @Test
    void deleteAllAssociations_noAssociations() {
        UUID accountId = UUID.randomUUID();

        when(itemAssociationService.findOrigins(ITEM_A_ID)).thenReturn(Flux.empty());
        when(itemAssociationService.findDestinations(ITEM_A_ID)).thenReturn(Flux.empty());

        List<ItemAssociation> deleted = documentItemService.deleteAllAssociations(ITEM_A_ID, accountId)
                .collectList()
                .block();

        assertNotNull(deleted);
        assertEquals(0, deleted.size());

        verify(itemAssociationService, never()).delete(any(ItemAssociation.class), eq(accountId));
    }
}
