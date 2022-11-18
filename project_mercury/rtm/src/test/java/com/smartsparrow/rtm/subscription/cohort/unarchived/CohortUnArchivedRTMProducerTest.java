package com.smartsparrow.rtm.subscription.cohort.unarchived;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class CohortUnArchivedRTMProducerTest {

    @InjectMocks
    private CohortUnArchivedRTMProducer cohortUnArchivedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID cohortId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        cohortUnArchivedRTMProducer.buildCohortUnArchivedRTMConsumable(rtmClientContext, cohortId);
        assertNotNull(cohortUnArchivedRTMProducer.getEventConsumable());
    }

}
