package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
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
import com.smartsparrow.rtm.subscription.courseware.message.AssetOptimizedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class AssetOptimizedRTMConsumableTest {
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID assetId = UUIDs.timeBased();
    private static final String assetUrl = "http://test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        AssetOptimizedBroadcastMessage message = new AssetOptimizedBroadcastMessage(activityId, activityId, ACTIVITY, assetId, assetUrl);
        AssetOptimizedRTMConsumable consumable = new AssetOptimizedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new AssetOptimizedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity/%s/%s",
                                   activityId,
                                   new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent()).getName(
                                           ACTIVITY)), consumable.getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
