package com.smartsparrow.rtm.subscription.cohort.granted;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.cohort.disenrolled.CohortDisEnrolledRTMConsumable;
import com.smartsparrow.rtm.subscription.cohort.disenrolled.CohortDisEnrolledRTMEvent;
import com.smartsparrow.util.UUIDs;

class CohortGrantedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cohort_granted() {
        CohortBroadcastMessage message = new CohortBroadcastMessage(cohortId);
        CohortGrantedRTMConsumable consumable = new CohortGrantedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new CohortGrantedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(CohortRTMSubscription.NAME(cohortId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
