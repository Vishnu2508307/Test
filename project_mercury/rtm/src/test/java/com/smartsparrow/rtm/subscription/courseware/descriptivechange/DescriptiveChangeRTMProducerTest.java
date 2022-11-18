package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class DescriptiveChangeRTMProducerTest {

    @InjectMocks
    private DescriptiveChangeRTMProducer descriptiveChangeRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final String value = "foo";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        descriptiveChangeRTMProducer.buildDescriptiveChangeRTMConsumable(rtmClientContext, activityId, activityId, ACTIVITY, value);
        assertNotNull(descriptiveChangeRTMProducer.getEventConsumable());
    }

}
