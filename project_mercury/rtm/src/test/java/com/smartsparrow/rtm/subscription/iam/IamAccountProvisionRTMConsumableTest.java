package com.smartsparrow.rtm.subscription.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.cohort.archived.CohortArchivedRTMEvent;
import com.smartsparrow.util.UUIDs;

class IamAccountProvisionRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID subscriptionId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changed() {
        IamAccountBroadcastMessage message = new IamAccountBroadcastMessage(subscriptionId,accountId);
        IamAccountProvisionRTMConsumable consumable = new IamAccountProvisionRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new IamAccountProvisionRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(IamAccountProvisionRTMSubscription.NAME(subscriptionId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
