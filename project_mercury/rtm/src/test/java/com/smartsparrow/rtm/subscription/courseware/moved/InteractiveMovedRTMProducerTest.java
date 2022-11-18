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

class InteractiveMovedRTMProducerTest {

    @InjectMocks
    private InteractiveMovedRTMProducer interactiveMovedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID fromPathwayId = UUIDs.timeBased();
    private static final UUID toPathwayId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        interactiveMovedRTMProducer.buildInteractiveMovedRTMConsumable(rtmClientContext, activityId, interactiveId, fromPathwayId, toPathwayId);
        assertNotNull(interactiveMovedRTMProducer.getEventConsumable());
    }

}
