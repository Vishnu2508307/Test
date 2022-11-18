package com.smartsparrow.rtm.subscription.courseware.scenarioreordered;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_COMPLETE;
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
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioReOrderedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class ScenarioReOrderedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final ScenarioLifecycle lifecycle = ACTIVITY_COMPLETE;
    private static final UUID idOne = UUIDs.timeBased();
    private static final UUID idTwo = UUIDs.timeBased();
    private static final List<UUID> scenarioIds = Lists.newArrayList(idOne, idTwo);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ScenarioReOrderedBroadcastMessage message = new ScenarioReOrderedBroadcastMessage(activityId,
                                                                                          activityId,
                                                                                          ACTIVITY,
                                                                                          scenarioIds,
                                                                                          lifecycle);
        ScenarioReOrderedRTMConsumable consumable = new ScenarioReOrderedRTMConsumable(rtmClientContext,
                                                                                       message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ScenarioReOrderedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
