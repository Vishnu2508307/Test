package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ThemeBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class ElementThemeCreateRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID themeId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ThemeBroadcastMessage message = new ThemeBroadcastMessage(activityId, activityId, ACTIVITY, themeId);
        ElementThemeCreateRTMConsumable consumable = new ElementThemeCreateRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ElementThemeCreateRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity/%s/%s",
                                   activityId,
                                   new ElementThemeCreateRTMEventDecoratorImpl(new ElementThemeCreateRTMEvent()).getName(
                                           ACTIVITY)), consumable.getName());
        assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
