package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_COMPLETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioCreatedBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class ScenarioCreatedRTMConsumableTest {
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID scenarioId = UUIDs.timeBased();
    private static final UUID parentElementId = UUIDs.timeBased();
    private static final CoursewareElementType parentElementType = SCENARIO;
    private static final ScenarioLifecycle lifecycle = ACTIVITY_COMPLETE;
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ScenarioCreatedBroadcastMessage message = new ScenarioCreatedBroadcastMessage(activityId, scenarioId, parentElementId, parentElementType, lifecycle);
        ScenarioCreatedRTMConsumable consumable = new ScenarioCreatedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ScenarioCreatedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
