package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class ActivityAssetRemovedRTMConsumableTest {
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID feedbackId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ActivityCreatedBroadcastMessage message = new ActivityCreatedBroadcastMessage(rootElementId, feedbackId, parentPathwayId);
        ActivityAssetRemovedRTMConsumable consumable = new ActivityAssetRemovedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ActivityAssetRemovedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(rootElementId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
