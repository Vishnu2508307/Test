package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

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

class AssetOptimizedRTMProducerTest {
    @InjectMocks
    private AssetOptimizedRTMProducer assetOptimizedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    private static final UUID assetId = UUIDs.timeBased();
    private static final String assetUrl = "http://test.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        assetOptimizedRTMProducer.buildAssetOptimizedRTMConsumable(rtmClientContext, activityId, elementId, ACTIVITY, assetId, assetUrl);
        assertNotNull(assetOptimizedRTMProducer.getEventConsumable());
    }
}
