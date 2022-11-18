package com.smartsparrow.rtm.subscription.courseware.annotationupdated;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;
import com.smartsparrow.util.UUIDs;

class AnnotationUpdatedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID annotationId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        AnnotationBroadcastMessage message = new AnnotationBroadcastMessage(activityId, activityId, ACTIVITY, annotationId);
        AnnotationUpdatedRTMConsumable consumable = new AnnotationUpdatedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new AnnotationUpdatedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("author.activity/%s/%s",
                                   activityId,
                                   new AnnotationUpdatedRTMEventDecoratorImpl(new AnnotationUpdatedRTMEvent()).getName(
                                           ACTIVITY)), consumable.getName());
        Assertions.assertEquals(ActivityRTMSubscription.NAME(activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
