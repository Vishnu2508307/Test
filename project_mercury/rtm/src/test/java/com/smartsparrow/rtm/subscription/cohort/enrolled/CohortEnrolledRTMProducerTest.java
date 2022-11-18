package com.smartsparrow.rtm.subscription.cohort.enrolled;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class CohortEnrolledRTMProducerTest {

    @InjectMocks
    private CohortEnrolledRTMProducer cohortEnrolledRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        cohortEnrolledRTMProducer.buildCohortEnrolledRTMConsumable(rtmClientContext, cohortId);
        assertNotNull(cohortEnrolledRTMProducer.getEventConsumable());
    }

}
