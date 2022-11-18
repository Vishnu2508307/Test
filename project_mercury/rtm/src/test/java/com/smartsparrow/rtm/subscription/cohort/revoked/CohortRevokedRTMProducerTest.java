package com.smartsparrow.rtm.subscription.cohort.revoked;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class CohortRevokedRTMProducerTest {

    @InjectMocks
    private CohortRevokedRTMProducer cohortRevokedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        cohortRevokedRTMProducer.buildCohortRevokedRTMConsumable(rtmClientContext, cohortId);
        assertNotNull(cohortRevokedRTMProducer.getEventConsumable());
    }

}
