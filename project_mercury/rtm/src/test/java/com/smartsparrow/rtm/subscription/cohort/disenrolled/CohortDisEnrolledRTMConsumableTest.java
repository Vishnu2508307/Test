package com.smartsparrow.rtm.subscription.cohort.disenrolled;

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
import com.smartsparrow.util.UUIDs;

class CohortDisEnrolledRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cohort_disenroll() {
        CohortBroadcastMessage message = new CohortBroadcastMessage(cohortId);
        CohortDisEnrolledRTMConsumable consumable = new CohortDisEnrolledRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new CohortDisEnrolledRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(CohortRTMSubscription.NAME(cohortId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
