package com.smartsparrow.rtm.subscription.courseware.moved;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ActivityMovedRTMProducerTest {

    @InjectMocks
    private ActivityMovedRTMProducer activityMovedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID fromPathwayId = UUIDs.timeBased();
    private static final UUID toPathwayId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        activityMovedRTMProducer.buildActivityMovedRTMConsumable(rtmClientContext, rootElementId, activityId, fromPathwayId, toPathwayId);
        assertNotNull(activityMovedRTMProducer.getEventConsumable());
    }

}
