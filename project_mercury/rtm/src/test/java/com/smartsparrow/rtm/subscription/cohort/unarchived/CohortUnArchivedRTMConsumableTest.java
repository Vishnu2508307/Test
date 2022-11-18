package com.smartsparrow.rtm.subscription.cohort.unarchived;

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

class CohortUnArchivedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void cohort_unarchived() {
        CohortBroadcastMessage message = new CohortBroadcastMessage(cohortId);
        CohortUnArchivedRTMConsumable consumable = new CohortUnArchivedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new CohortUnArchivedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(CohortRTMSubscription.NAME(cohortId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
