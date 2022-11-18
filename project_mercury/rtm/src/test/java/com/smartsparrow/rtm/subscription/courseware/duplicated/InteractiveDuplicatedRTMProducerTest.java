package com.smartsparrow.rtm.subscription.courseware.duplicated;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class InteractiveDuplicatedRTMProducerTest {

    @InjectMocks
    private InteractiveDuplicatedRTMProducer interactiveDuplicatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        interactiveDuplicatedRTMProducer.buildInteractiveDuplicatedRTMConsumable(rtmClientContext, activityId, interactiveId, parentPathwayId);
        assertNotNull(interactiveDuplicatedRTMProducer.getEventConsumable());
    }

}
