package com.smartsparrow.rtm.subscription.cohort.changed;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class CohortChangedRTMProducerTest {

    @InjectMocks
    private CohortChangedRTMProducer cohortChangedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, cohortId);
        assertNotNull(cohortChangedRTMProducer.getEventConsumable());
    }

}
