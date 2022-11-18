package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class AssetsRemovedRTMProducerTest {

    @InjectMocks
    private AssetsRemovedRTMProducer assetsRemovedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        assetsRemovedRTMProducer.buildAssetsRemovedRTMConsumable(rtmClientContext, activityId, interactiveId, INTERACTIVE);
        assertNotNull(assetsRemovedRTMProducer.getEventConsumable());
    }

}
