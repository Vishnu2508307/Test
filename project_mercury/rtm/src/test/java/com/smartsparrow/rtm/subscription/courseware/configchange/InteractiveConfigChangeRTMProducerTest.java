package com.smartsparrow.rtm.subscription.courseware.configchange;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class InteractiveConfigChangeRTMProducerTest {

    @InjectMocks
    private InteractiveConfigChangeRTMProducer interactiveConfigChangeRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final String config = "[{'foo': 'bar'}]";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        interactiveConfigChangeRTMProducer.buildInteractiveConfigChangeRTMConsumable(rtmClientContext, activityId, interactiveId, config);
        assertNotNull(interactiveConfigChangeRTMProducer.getEventConsumable());
    }

}
