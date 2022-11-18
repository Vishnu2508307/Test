package com.smartsparrow.competency.service;

import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B;
import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_A_B_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ASSOCIATION_B_A;
import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B_ID;
import static com.smartsparrow.iam.IamDataStub.INSTRUCTOR_A_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.data.ItemAssociationGateway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.data.LearnerDocumentItemAssociationGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ItemAssociationServiceTest {

    @InjectMocks
    private ItemAssociationService itemAssociationService;

    @Mock
    private ItemAssociationGateway itemAssociationGateway;
    @Mock
    private DocumentService documentService;
    @Mock
    private LearnerDocumentItemAssociationGateway learnerDocumentItemAssociationGateway;

    private TestPublisher<Void> versionUpdatePublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        versionUpdatePublisher = TestPublisher.<Void>create().complete();
        when(documentService.updateVersion(DOCUMENT_A_ID, INSTRUCTOR_A_ID)).thenReturn(versionUpdatePublisher.flux());

        when(learnerDocumentItemAssociationGateway.findById(any())).thenReturn(Mono.empty());
    }

    @Test
    void create() {
        when(itemAssociationGateway.persist(any())).thenReturn(Flux.empty());

        ItemAssociation result = itemAssociationService.create(DOCUMENT_A_ID, ITEM_A_ID, ITEM_B_ID,
                AssociationType.IS_CHILD_OF, INSTRUCTOR_A_ID).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertEquals(DOCUMENT_A_ID, result.getDocumentId());
        assertEquals(ITEM_A_ID, result.getOriginItemId());
        assertEquals(ITEM_B_ID, result.getDestinationItemId());
        assertEquals(AssociationType.IS_CHILD_OF, result.getAssociationType());
        assertEquals(INSTRUCTOR_A_ID, result.getCreatedById());
        verify(itemAssociationGateway).persist(result);
        versionUpdatePublisher.assertWasRequested();
    }

    @Test
    void delete() {
        when(itemAssociationGateway.delete(any())).thenReturn(Flux.empty());

        ItemAssociation result = itemAssociationService.delete(ASSOCIATION_A_B, INSTRUCTOR_A_ID).block();

        assertEquals(ASSOCIATION_A_B, result);
        verify(itemAssociationGateway).delete(ASSOCIATION_A_B);
        versionUpdatePublisher.assertWasRequested();
    }

    @Test
    void delete_publishedAssociation() {
        TestPublisher<Void> deletePublisher = versionUpdatePublisher = TestPublisher.<Void>create().complete();
        when(itemAssociationGateway.delete(any())).thenReturn(deletePublisher.flux());
        when(learnerDocumentItemAssociationGateway.findById(any())).thenReturn(Mono.just(new ItemAssociation().setId(ASSOCIATION_A_B_ID)));

        assertThrows(IllegalArgumentFault.class, () -> itemAssociationService.delete(ASSOCIATION_A_B, INSTRUCTOR_A_ID).block());

        versionUpdatePublisher.assertWasNotRequested();
        deletePublisher.assertWasNotRequested();
    }

    @Test
    void findById() {
        when(itemAssociationGateway.findById(ASSOCIATION_A_B_ID)).thenReturn(Mono.just(ASSOCIATION_A_B));

        ItemAssociation result = itemAssociationService.findById(ASSOCIATION_A_B_ID).block();

        assertEquals(ASSOCIATION_A_B, result);
    }

    @Test
    void findById_notFound() {
        when(itemAssociationGateway.findById(ASSOCIATION_A_B_ID)).thenReturn(Mono.empty());

        NotFoundFault t = assertThrows(NotFoundFault.class,
                () -> itemAssociationService.findById(ASSOCIATION_A_B_ID).block());
        assertEquals("Association " + ASSOCIATION_A_B_ID + " does not exist", t.getMessage());
    }

    @Test
    void findAllAssociations_noOrigins() {
        when(itemAssociationGateway.findOrigins(ITEM_A_ID)).thenReturn(Flux.empty());
        when(itemAssociationGateway.findDestinations(ITEM_A_ID)).thenReturn(Flux.just(ASSOCIATION_B_A));

        List<ItemAssociation> found = itemAssociationService.findAllAssociations(ITEM_A_ID)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(1, found.size());
    }

    @Test
    void findAllAssociations_noDestinations() {
        when(itemAssociationGateway.findOrigins(ITEM_A_ID)).thenReturn(Flux.just(ASSOCIATION_B_A));
        when(itemAssociationGateway.findDestinations(ITEM_A_ID)).thenReturn(Flux.empty());

        List<ItemAssociation> found = itemAssociationService.findAllAssociations(ITEM_A_ID)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(1, found.size());
    }

    @Test
    void findAllAssociations_noneFound() {
        when(itemAssociationGateway.findOrigins(ITEM_A_ID)).thenReturn(Flux.empty());
        when(itemAssociationGateway.findDestinations(ITEM_A_ID)).thenReturn(Flux.empty());

        List<ItemAssociation> found = itemAssociationService.findAllAssociations(ITEM_A_ID)
                .collectList()
                .block();

        assertNotNull(found);
        assertTrue(found.isEmpty());
    }
}
