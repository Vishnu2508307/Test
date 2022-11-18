package com.smartsparrow.rtm.subscription.competency.association.deleted;

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

public class CompetencyItemAssociationDeletedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID documentId = UUIDs.timeBased();
    private static final UUID associationId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        CompetencyDocumentBroadcastMessage message = new CompetencyDocumentBroadcastMessage().setDocumentId(documentId).setAssociationId(associationId);
        CompetencyItemAssociationDeletedRTMConsumable consumable = new CompetencyItemAssociationDeletedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new CompetencyItemAssociationDeletedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(CompetencyDocumentEventRTMSubscription.NAME(documentId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
