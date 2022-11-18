package com.smartsparrow.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerDocumentItem;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.payload.LearnerDocumentItemAssociationPayload;
import com.smartsparrow.learner.payload.LearnerDocumentItemPayload;
import com.smartsparrow.learner.payload.LearnerDocumentPayload;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;

import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentItemSchemaTest {

    @InjectMocks
    DocumentItemSchema documentItemSchema;

    @Mock
    DocumentItemService documentItemService;

    @Mock
    ItemAssociationService itemAssociationService;

    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    private UUID documentId1 = UUID.randomUUID();
    private UUID documentItemId1 = UUID.randomUUID();
    private UUID documentItemId2 = UUID.randomUUID();

    private DocumentItem documentItem1, documentItem2;
    private ItemAssociation itemAssociation1, itemAssociation2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        documentItem1 = new DocumentItem()
                .setAbbreviatedStatement("blah1")
                .setFullStatement("full1")
                .setHumanCodingScheme("human1")
                .setDocumentId(documentId1)
                .setId(documentItemId1)
                .setModifiedAt(UUID.randomUUID())
                .setModifiedById(UUID.randomUUID())
                .setCreatedAt(UUID.randomUUID())
                .setCreatedById(UUID.randomUUID());

        documentItem2 = new DocumentItem()
                .setAbbreviatedStatement("blah2")
                .setFullStatement("full2")
                .setHumanCodingScheme("human2")
                .setDocumentId(documentId1)
                .setId(documentItemId2)
                .setModifiedAt(UUID.randomUUID())
                .setModifiedById(UUID.randomUUID())
                .setCreatedAt(UUID.randomUUID())
                .setCreatedById(UUID.randomUUID());

        itemAssociation1 = new ItemAssociation()
                .setAssociationType(AssociationType.PRECEDES)
                .setDestinationItemId(documentItemId2)
                .setOriginItemId(documentItemId1)
                .setCreatedAt(UUIDs.timeBased())
                .setDocumentId(documentId1)
                .setId(UUID.randomUUID());

        itemAssociation2 = new ItemAssociation()
                .setAssociationType(AssociationType.IS_CHILD_OF)
                .setDestinationItemId(documentItemId1)
                .setOriginItemId(documentItemId2)
                .setCreatedAt(UUIDs.timeBased())
                .setDocumentId(documentId1)
                .setId(UUID.randomUUID());


    }

    @Test
    void getDocumentItems_NoDocument() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            documentItemSchema.getDocumentItems(null, documentId1, null, null).join();
        });
        assertEquals("document is required", e.getMessage());
    }

    @Test
    void getDocumentItems_NoItems() {
        when(documentItemService.findById(any(UUID.class)))
                .thenReturn(Mono.empty());
        Page<DocumentItem> documentItems = documentItemSchema
                .getDocumentItems(new Document(), documentId1, null, null).join();

        assertNotNull(documentItems);
        assertNotNull(documentItems.getEdges());
        assertEquals(0, documentItems.getEdges().size());
    }

    @Test
    void getDocumentItems_ItemAvailable() {
        when(documentItemService.findById(any(UUID.class)))
                .thenReturn(Mono.just(documentItem1));
        Page<DocumentItem> documentItems = documentItemSchema
                .getDocumentItems(new Document(), documentId1, null, null).join();
        assertNotNull(documentItems);
        assertNotNull(documentItems.getEdges());
        assertEquals(1, documentItems.getEdges().size());
        assertNotNull(documentItems.getEdges().get(0));
        assertNotNull(documentItems.getEdges().get(0).getNode());
        assertEquals(documentItem1, documentItems.getEdges().get(0).getNode());
    }

    @Test
    void getDocumentItems_FromContext() {
        when(documentItemService.findByDocumentId(any(UUID.class)))
                .thenReturn(Flux.just(documentItem1, documentItem2));
        Page<DocumentItem> documentItems = documentItemSchema
                .getDocumentItems(new Document()
                        .setId(documentId1), null, null, null).join();
        assertNotNull(documentItems);
        assertNotNull(documentItems.getEdges());
        assertEquals(2, documentItems.getEdges().size());
        assertNotNull(documentItems.getEdges().get(0));
        assertNotNull(documentItems.getEdges().get(0).getNode());
        assertEquals(documentItem1, documentItems.getEdges().get(0).getNode());
        assertNotNull(documentItems.getEdges().get(1));
        assertNotNull(documentItems.getEdges().get(1).getNode());
        assertEquals(documentItem2, documentItems.getEdges().get(1).getNode());
    }


    @Test
    void getOriginAssociations_documentItemNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            documentItemSchema.getOriginAssociations(null, null, null, null).join();
        });
        assertEquals("documentItem is required", e.getMessage());
    }

    @Test
    void getOriginAssociations_NoAssociationTypeFilter() {
        when(itemAssociationService.findOrigins(any(UUID.class)))
                .thenReturn(Flux.empty());

        Page<ItemAssociation> originAssociations = documentItemSchema.getOriginAssociations(new DocumentItem()
                .setId(documentItemId1), null, null, null).join();

        assertNotNull(originAssociations);
        assertNotNull(originAssociations.getEdges());
        assertEquals(0, originAssociations.getEdges().size());
    }

    @Test
    void getOriginAssociations_withAssociationType() {
        when(itemAssociationService.findOrigins(any(UUID.class), any(AssociationType.class)))
                .thenReturn(Flux.empty());

        Page<ItemAssociation> originAssociations = documentItemSchema.getOriginAssociations(new DocumentItem()
                .setId(documentItemId1), AssociationType.PRECEDES, null, null).join();

        assertNotNull(originAssociations);
        assertNotNull(originAssociations.getEdges());
        assertEquals(0, originAssociations.getEdges().size());
    }

    @Test
    void getOriginAssociations_validAssociations() {
        when(itemAssociationService.findOrigins(any(UUID.class)))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<ItemAssociation> originAssociations = documentItemSchema.getOriginAssociations(new DocumentItem()
                .setId(documentItemId1), null, null, null).join();

        assertNotNull(originAssociations);
        assertNotNull(originAssociations.getEdges());
        assertEquals(2, originAssociations.getEdges().size());
        assertNotNull(originAssociations.getEdges().get(0));
        assertNotNull(originAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, originAssociations.getEdges().get(0).getNode());
        assertNotNull(originAssociations.getEdges().get(1));
        assertNotNull(originAssociations.getEdges().get(1).getNode());
        assertEquals(itemAssociation2, originAssociations.getEdges().get(1).getNode());
    }

    @Test
    void getOriginAssociations_WithAssociationType_validAssociations() {
        when(itemAssociationService.findOrigins(any(UUID.class), any(AssociationType.class)))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<ItemAssociation> originAssociations = documentItemSchema.getOriginAssociations(new DocumentItem()
                .setId(documentItemId1), AssociationType.PRECEDES, null, null).join();

        assertNotNull(originAssociations);
        assertNotNull(originAssociations.getEdges());
        assertEquals(2, originAssociations.getEdges().size());
        assertNotNull(originAssociations.getEdges().get(0));
        assertNotNull(originAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, originAssociations.getEdges().get(0).getNode());
        assertNotNull(originAssociations.getEdges().get(1));
        assertNotNull(originAssociations.getEdges().get(1).getNode());
        assertEquals(itemAssociation2, originAssociations.getEdges().get(1).getNode());
    }

    @Test
    void getDestinationAssociations_documentItemNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> {
            documentItemSchema.getOriginAssociations(null, null, null, null).join();
        });
        assertEquals("documentItem is required", e.getMessage());

    }

    @Test
    void getDestinationAssociations_NoAssociationTypeFilter() {
        when(itemAssociationService.findDestinations(any(UUID.class)))
                .thenReturn(Flux.empty());

        Page<ItemAssociation> destinationAssociations = documentItemSchema
                .getDestinationAssociations(new DocumentItem()
                        .setId(documentItemId1), null, null, null).join();

        assertNotNull(destinationAssociations);
        assertNotNull(destinationAssociations.getEdges());
        assertEquals(0, destinationAssociations.getEdges().size());
    }

    @Test
    void getDestinationAssociations_withAssociationType() {
        when(itemAssociationService.findDestinations(any(UUID.class), any(AssociationType.class)))
                .thenReturn(Flux.empty());

        Page<ItemAssociation> destinationAssociations = documentItemSchema.getDestinationAssociations(new DocumentItem()
                .setId(documentItemId1), AssociationType.PRECEDES, null, null).join();

        assertNotNull(destinationAssociations);
        assertNotNull(destinationAssociations.getEdges());
        assertEquals(0, destinationAssociations.getEdges().size());
    }

    @Test
    void getDestinationAssociations_validAssociations() {
        when(itemAssociationService.findDestinations(any(UUID.class)))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<ItemAssociation> destinationAssociations = documentItemSchema.getDestinationAssociations(new DocumentItem()
                .setId(documentItemId1), null, null, null).join();

        assertNotNull(destinationAssociations);
        assertNotNull(destinationAssociations.getEdges());
        assertEquals(2, destinationAssociations.getEdges().size());
        assertNotNull(destinationAssociations.getEdges().get(0));
        assertNotNull(destinationAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, destinationAssociations.getEdges().get(0).getNode());
        assertNotNull(destinationAssociations.getEdges().get(1));
        assertNotNull(destinationAssociations.getEdges().get(1).getNode());
        assertEquals(itemAssociation2, destinationAssociations.getEdges().get(1).getNode());
    }

    @Test
    void getDestinationAssociations_WithAssociationType_validAssociations() {
        when(itemAssociationService.findDestinations(any(UUID.class), any(AssociationType.class)))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<ItemAssociation> destinationAssociations = documentItemSchema.getDestinationAssociations(new DocumentItem()
                .setId(documentItemId1), AssociationType.PRECEDES, null, null).join();

        assertNotNull(destinationAssociations);
        assertNotNull(destinationAssociations.getEdges());
        assertEquals(2, destinationAssociations.getEdges().size());
        assertNotNull(destinationAssociations.getEdges().get(0));
        assertNotNull(destinationAssociations.getEdges().get(0).getNode());
        assertEquals(itemAssociation1, destinationAssociations.getEdges().get(0).getNode());
        assertNotNull(destinationAssociations.getEdges().get(1));
        assertNotNull(destinationAssociations.getEdges().get(1).getNode());
        assertEquals(itemAssociation2, destinationAssociations.getEdges().get(1).getNode());
    }

    @Test
    void getLinkedItems_noneFound() {
        LearnerWalkable walkable = mock(LearnerWalkable.class);
        when(learnerCompetencyDocumentService.findLinkedItems(any(LearnerWalkable.class))).thenReturn(Flux.empty());

        Page<LearnerDocumentItemPayload> result = documentItemSchema
                .getLinkedItems(walkable, null, null)
                .join();

        assertNotNull(result);
        assertEquals(0, result.getEdges().size());
    }

    @Test
    void getLinkedItems() {
        LearnerWalkable walkable = mock(LearnerWalkable.class);
        when(learnerCompetencyDocumentService.findLinkedItems(walkable))
                .thenReturn(Flux.just(new LearnerDocumentItem()));

        Page<LearnerDocumentItemPayload> result = documentItemSchema
                .getLinkedItems(walkable, null, null)
                .join();
        assertNotNull(result);
        assertEquals(1, result.getEdges().size());
    }


    @Test
    void getDocumentItems() {
        LearnerDocumentPayload payload = mock(LearnerDocumentPayload.class);
        when(payload.getDocumentId()).thenReturn(documentId1);

        LearnerDocumentItem learnerDocumentItem = new LearnerDocumentItem()
                .setDocumentId(documentId1)
                .setCreatedAt(UUIDs.timeBased());

        when(learnerCompetencyDocumentService.findItems(documentId1)).thenReturn(Flux.just(learnerDocumentItem));

        Page<LearnerDocumentItemPayload> found = documentItemSchema
                .getDocumentItems(payload, null, null)
                .join();

        assertNotNull(found);
        assertEquals(1, found.getEdges().size());
    }

    @Test
    void getLearnerOriginAssosiactions_all() {
        LearnerDocumentItemPayload payload = mock(LearnerDocumentItemPayload.class);
        when(payload.getId()).thenReturn(documentItemId1);

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId1))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<LearnerDocumentItemAssociationPayload> associationsPage = documentItemSchema
                .getLearnerOriginAssociations(payload, null, null, null)
                .join();

        assertNotNull(associationsPage);
        assertEquals(2, associationsPage.getEdges().size());
    }

    @Test
    void getLearnerOriginAssociations_byType() {
        LearnerDocumentItemPayload payload = mock(LearnerDocumentItemPayload.class);
        when(payload.getId()).thenReturn(documentItemId1);

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId1, AssociationType.PRECEDES))
                .thenReturn(Flux.just(itemAssociation1));

        Page<LearnerDocumentItemAssociationPayload> associationsPage = documentItemSchema
                .getLearnerOriginAssociations(payload, AssociationType.PRECEDES, null, null)
                .join();

        assertNotNull(associationsPage);
        assertEquals(1, associationsPage.getEdges().size());
    }

    @Test
    void getLearnerDestinationAssociations_all() {
        LearnerDocumentItemPayload payload = mock(LearnerDocumentItemPayload.class);
        when(payload.getId()).thenReturn(documentItemId1);

        when(learnerCompetencyDocumentService.findAssociationsTo(documentItemId1))
                .thenReturn(Flux.just(itemAssociation1, itemAssociation2));

        Page<LearnerDocumentItemAssociationPayload> associationsPage = documentItemSchema
                .getLearnerDestinationAssociations(payload, null, null, null)
                .join();

        assertNotNull(associationsPage);
        assertEquals(2, associationsPage.getEdges().size());
    }

    @Test
    void getLearnerDestinationAssociations_by_type() {
        LearnerDocumentItemPayload payload = mock(LearnerDocumentItemPayload.class);
        when(payload.getId()).thenReturn(documentItemId1);

        when(learnerCompetencyDocumentService.findAssociationsTo(documentItemId1, AssociationType.PRECEDES))
                .thenReturn(Flux.just(itemAssociation1));

        Page<LearnerDocumentItemAssociationPayload> associationsPage = documentItemSchema
                .getLearnerDestinationAssociations(payload, AssociationType.PRECEDES, null, null)
                .join();

        assertNotNull(associationsPage);
        assertEquals(1, associationsPage.getEdges().size());
    }
}
