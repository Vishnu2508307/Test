package com.smartsparrow.rtm.subscription.courseware.manualgrading;

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
import com.smartsparrow.rtm.subscription.courseware.message.ComponentManualGradingBroadcastMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ManualGradingConfig;
import com.smartsparrow.util.UUIDs;

class ComponentConfigurationCreatedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID componentId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ComponentManualGradingBroadcastMessage message = new ComponentManualGradingBroadcastMessage(activityId,
                                                                                                    componentId,
                                                                                                    new ManualGradingConfig().setComponentId(
                                                                                                            componentId));
        ComponentConfigurationCreatedRTMConsumable consumable = new ComponentConfigurationCreatedRTMConsumable(
                rtmClientContext,
                message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ComponentConfigurationCreatedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
