package com.smartsparrow.rtm.subscription.courseware.deleted;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class FeedbackDeletedRTMProducerTest {

    @InjectMocks
    private FeedbackDeletedRTMProducer feedbackDeletedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID feedbackId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        feedbackDeletedRTMProducer.buildFeedbackDeletedRTMConsumable(rtmClientContext, activityId, feedbackId);
        assertNotNull(feedbackDeletedRTMProducer.getEventConsumable());
    }

}
