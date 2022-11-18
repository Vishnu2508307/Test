package com.smartsparrow.rtm.subscription.competency.deleted;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.util.UUIDs;

public class DocumentItemDeletedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID documentId = UUIDs.timeBased();
    private static final UUID documentItemId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        CompetencyDocumentBroadcastMessage message = new CompetencyDocumentBroadcastMessage().setDocumentId(documentId).setDocumentItemId(documentItemId);
        DocumentItemDeletedRTMConsumable consumable = new DocumentItemDeletedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new DocumentItemDeletedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(CompetencyDocumentEventRTMSubscription.NAME(documentId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
