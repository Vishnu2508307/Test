package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.EvaluationMode.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class EvaluableSetRTMProducerTest {

    @InjectMocks
    private EvaluableSetRTMProducer evaluableSetRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        evaluableSetRTMProducer.buildEvaluableSetRTMConsumable(rtmClientContext, activityId, activityId, ACTIVITY, DEFAULT);
        assertNotNull(evaluableSetRTMProducer.getEventConsumable());
    }

}
