package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayReOrderedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class PathwayReOrderedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID idOne = UUIDs.timeBased();
    private static final UUID idTwo = UUIDs.timeBased();
    private static final List<WalkableChild> walkables = Lists.newArrayList(new WalkableChild().setElementId(idOne),
                                                                            new WalkableChild().setElementId(idTwo));
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        PathwayReOrderedBroadcastMessage message = new PathwayReOrderedBroadcastMessage(activityId,
                                                                                        pathwayId,
                                                                                        walkables);
        PathwayReOrderedRTMConsumable consumable = new PathwayReOrderedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new PathwayReOrderedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
