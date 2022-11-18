package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
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
import com.smartsparrow.rtm.subscription.courseware.message.AssetRemovedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class AssetRemovedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        AssetRemovedBroadcastMessage message = new AssetRemovedBroadcastMessage(activityId, interactiveId, INTERACTIVE);
        AssetRemovedRTMConsumable consumable = new AssetRemovedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new AssetRemovedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity/%s/%s",
                                   activityId,
                                   new AssetRemovedRTMEventDecoratorImpl(new AssetRemovedRTMEvent()).getName(
                                           INTERACTIVE)), consumable.getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
