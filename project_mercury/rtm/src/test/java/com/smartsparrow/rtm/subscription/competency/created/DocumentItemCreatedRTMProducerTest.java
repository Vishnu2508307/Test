package com.smartsparrow.rtm.subscription.competency.created;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

public class DocumentItemCreatedRTMProducerTest {

    @InjectMocks
    private DocumentItemCreatedRTMProducer documentItemCreatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID documentId = UUIDs.timeBased();
    private static final UUID documentItemId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        documentItemCreatedRTMProducer.buildDocumentItemCreatedRTMConsumable(rtmClientContext, documentId, documentItemId);
        assertNotNull(documentItemCreatedRTMProducer.getEventConsumable());
    }
}
