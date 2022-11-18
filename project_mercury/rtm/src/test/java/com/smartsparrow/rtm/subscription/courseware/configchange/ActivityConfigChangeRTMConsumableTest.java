package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
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
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class ActivityConfigChangeRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final String config = "[{'foo': 'bar'}]";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ConfigChangeBroadcastMessage message = new ConfigChangeBroadcastMessage(activityId, activityId, ACTIVITY, config);
        ActivityConfigChangeRTMConsumable consumable = new ActivityConfigChangeRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ActivityConfigChangeRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
