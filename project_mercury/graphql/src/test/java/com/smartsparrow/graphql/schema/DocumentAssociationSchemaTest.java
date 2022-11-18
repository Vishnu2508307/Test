package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.exception.IllegalArgumentFault;

import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentAssociationSchemaTest {

    @InjectMocks
    DocumentAssociationSchema documentAssociationSchema;

    @Mock
    ItemAssociationService itemAssociationService;

    private ItemAssociation itemAssociation1, itemAssociation2;
    private UUID documentItemId1 = UUID.randomUUID();
    private UUID documentItemId2 = UUID.randomUUID();
    private UUID documentId1 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        itemAssociation1 = new ItemAssociation()
                .setAssociationType(AssociationType.PRECEDES)
                .setDestinationItemId(documentItemId2)
                .setOriginItemId(documentItemId1)
                .setCreatedAt(UUID.randomUUID())
                .setDocumentId(documentId1)
                .setId(UUID.randomUUID());

        itemAssociation2 = new ItemAssociation()
                .setAssociationType(AssociationType.IS_CHILD_OF)
                .setDestinationItemId(documentItemId1)
                .setOriginItemId(documentItemId2)
                .setCreatedAt(UUID.randomUUID())
                .setDocumentId(documentId1)
                .setId(UUID.randomUUID());

    }

    @Test
    void getDocumentAssociations_NoDocument() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            documentAssociationSchema
                    .getDocumentItemAssociations(null, null, null, null)
                    .join();
        });
        assertEquals("document is required", e.getMessage());
    }

    @Test
    void getDocumentAssociations_FromContext_NoAssoc() {
        when(itemAssociationService.findAssociationByDocument(any(UUID.class))).thenReturn(Flux.empty());

        Page<ItemAssociation> documentItemAssociations = documentAssociationSchema
                .getDocumentItemAssociations(new Document()
                        .setId(documentId1), null, null, null)
                .join();

        assertNotNull(documentItemAssociations);
        assertNotNull(documentItemAssociations.getEdges());
        assertEquals(0, documentItemAssociations.getEdges().size());
    }

    @Test
    void getDocumentAssociations_FromParam_NoAssoc() {
        when(itemAssociationService.findById(any(UUID.class))).thenReturn(Mono.empty());

        Page<ItemAssociation> documentItemAssociations = documentAssociationSchema
                .getDocumentItemAssociations(new Document()
                        .setId(documentId1), UUID.randomUUID(), null, null)
                .join();

        assertNotNull(documentItemAssociations);
        assertNotNull(documentItemAssociations.getEdges());
        assertEquals(0, documentItemAssociations.getEdges().size());
    }

    @Test
    void getDocumentAssociations_FromContext_Valid() {
        when(itemAssociationService.findAssociationByDocument(any(UUID.class)))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));
        Page<ItemAssociation> documentItemAssociations = documentAssociationSchema
                .getDocumentItemAssociations(new Document()
                        .setId(documentId1), null, null, null)
                .join();

        assertNotNull(documentItemAssociations);
        assertNotNull(documentItemAssociations.getEdges());
        assertEquals(2, documentItemAssociations.getEdges().size());
        assertNotNull(documentItemAssociations.getEdges().get(0));
        assertNotNull(documentItemAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, documentItemAssociations.getEdges().get(0).getNode());
        assertNotNull(documentItemAssociations.getEdges().get(1));
        assertNotNull(documentItemAssociations.getEdges().get(1).getNode());
        assertEquals(itemAssociation2, documentItemAssociations.getEdges().get(1).getNode());
    }

    @Test
    void getDocumentAssociations_FromParam_Valid() {
        when(itemAssociationService.findById(itemAssociation1.getId()))
                .thenReturn(Mono.just(itemAssociation1));

        Page<ItemAssociation> documentItemAssociations = documentAssociationSchema
                .getDocumentItemAssociations(new Document()
                        .setId(documentId1), itemAssociation1.getId(), null, null)
                .join();

        assertNotNull(documentItemAssociations);
        assertNotNull(documentItemAssociations.getEdges());
        assertEquals(1, documentItemAssociations.getEdges().size());
        assertNotNull(documentItemAssociations.getEdges().get(0));
        assertNotNull(documentItemAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, documentItemAssociations.getEdges().get(0).getNode());

    }

}
