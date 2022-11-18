package com.smartsparrow.rtm.subscription.cohort;

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

class CohortRTMSubscriptionTest {

    @InjectMocks
    private CohortRTMSubscription subscription;
    @Mock
    private CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new CohortRTMSubscription(cohortId);
        when(cohortRTMSubscriptionFactory.create(cohortId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(CohortRTMSubscription.NAME(cohortId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("workspace.cohort.broadcast", subscription.getBroadcastType());
        assertEquals(CohortRTMSubscription.class, subscription.getSubscriptionType());
    }

}
