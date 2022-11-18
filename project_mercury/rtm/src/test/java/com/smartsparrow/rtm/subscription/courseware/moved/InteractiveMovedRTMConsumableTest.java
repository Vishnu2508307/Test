package com.smartsparrow.rtm.subscription.courseware.moved;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ElementMovedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class InteractiveMovedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID fromPathwayId = UUIDs.timeBased();
    private static final UUID toPathwayId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ElementMovedBroadcastMessage message = new ElementMovedBroadcastMessage(activityId,
                                                                                interactiveId,
                                                                                INTERACTIVE,
                                                                                fromPathwayId,
                                                                                toPathwayId);
        InteractiveMovedRTMConsumable consumable = new InteractiveMovedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new InteractiveMovedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
