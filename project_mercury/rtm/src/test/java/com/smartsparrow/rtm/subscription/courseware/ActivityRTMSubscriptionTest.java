package com.smartsparrow.rtm.subscription.courseware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class ActivityRTMSubscriptionTest {

    @InjectMocks
    private ActivityRTMSubscription subscription;

    @Mock
    private ActivityRTMSubscription.ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory;

    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new ActivityRTMSubscription(activityId);
        when(activityRTMSubscriptionFactory.create(activityId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(activityRTMSubscriptionFactory.create(activityId), subscription);
        assertEquals(ActivityRTMSubscription.NAME(activityId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("author.activity.broadcast", subscription.getBroadcastType());
        assertEquals(ActivityRTMSubscription.class, subscription.getSubscriptionType());
    }

}