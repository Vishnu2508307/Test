package com.smartsparrow.rtm.subscription.supplier.competency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;

import reactor.core.publisher.Mono;

class DocumentItemMutatedContentSupplierTest {

    @InjectMocks
    private DocumentItemMutatedContentSupplier supplier;

    @Mock
    private DocumentItemService documentItemService;

    private CompetencyDocumentBroadcastMessage content;
    private static final UUID documentItemId = UUID.randomUUID();
    private static final UUID documentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        content = mock(CompetencyDocumentBroadcastMessage.class);

        when(content.getDocumentItemId()).thenReturn(documentItemId);
    }

    @Test
    void supplyFrom() {
        DocumentItemPayload payload = DocumentItemPayload.from(new DocumentItem()
                .setId(documentItemId)
                .setFullStatement("full statement")
                .setAbbreviatedStatement("fs")
                .setHumanCodingScheme("scheme")
                .setDocumentId(documentId)
                .setModifiedAt(UUIDs.timeBased())
                .setCreatedAt(UUIDs.timeBased())
                .setModifiedById(UUID.randomUUID())
                .setCreatedById(UUID.randomUUID()));

        when(documentItemService.getDocumentItemPayload(documentItemId))
                .thenReturn(Mono.just(payload));

        DocumentItemPayload item = supplier.supplyFrom(content).block();

        assertNotNull(item);
        assertEquals("full statement", item.getFullStatement());
        assertEquals("fs", item.getAbbreviatedStatement());
        assertEquals("scheme", item.getHumanCodingScheme());
        assertEquals(documentId, item.getDocumentId());
        assertEquals(documentItemId, item.getId());
        assertNotNull(item.getCreatedAt());
    }
}
