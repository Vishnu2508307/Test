package com.smartsparrow.rtm.subscription.courseware.created;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class PathwayCreatedRTMProducerTest {

    @InjectMocks
    private PathwayCreatedRTMProducer pathwayCreatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        pathwayCreatedRTMProducer.buildPathwayCreatedRTMConsumable(rtmClientContext, activityId, pathwayId);
        assertNotNull(pathwayCreatedRTMProducer.getEventConsumable());
    }

}
