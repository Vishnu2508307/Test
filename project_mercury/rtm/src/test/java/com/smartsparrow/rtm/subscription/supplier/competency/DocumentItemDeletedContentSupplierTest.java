package com.smartsparrow.rtm.subscription.supplier.competency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;

class DocumentItemDeletedContentSupplierTest {

    @InjectMocks
    private DocumentItemDeletedContentSupplier supplier;

    private static final UUID documentId = UUID.randomUUID();
    private static final UUID id = UUID.randomUUID();

    @Mock
    private CompetencyDocumentBroadcastMessage content;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(content.getDocumentItemId()).thenReturn(id);
        when(content.getDocumentId()).thenReturn(documentId);
    }

    @Test
    void supplyFrom() {
        Map<String, Object> fields = supplier.supplyFrom(content).block();

        assertNotNull(fields);
        assertEquals(id, fields.get("id"));
        assertEquals(documentId, fields.get("documentId"));
    }

}
