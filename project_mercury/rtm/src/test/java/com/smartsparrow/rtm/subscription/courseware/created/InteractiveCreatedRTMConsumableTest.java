package com.smartsparrow.rtm.subscription.courseware.created;

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
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class InteractiveCreatedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID parentElementId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        InteractiveCreatedBroadcastMessage message = new InteractiveCreatedBroadcastMessage(activityId, interactiveId, parentElementId);
        InteractiveCreatedRTMConsumable consumable = new InteractiveCreatedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new InteractiveCreatedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
