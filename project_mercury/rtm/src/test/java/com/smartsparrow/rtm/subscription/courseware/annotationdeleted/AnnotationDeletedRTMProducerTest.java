package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

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

class AnnotationDeletedRTMProducerTest {

    @InjectMocks
    private AnnotationDeletedRTMProducer annotationDeletedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID annotationId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        annotationDeletedRTMProducer.buildAnnotationDeletedRTMConsumable(rtmClientContext, activityId, activityId, ACTIVITY, annotationId);
        assertNotNull(annotationDeletedRTMProducer.getEventConsumable());
    }

}
