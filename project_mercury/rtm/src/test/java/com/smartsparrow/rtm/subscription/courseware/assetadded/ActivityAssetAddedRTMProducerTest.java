package com.smartsparrow.rtm.subscription.courseware.assetadded;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ActivityAssetAddedRTMProducerTest {
    @InjectMocks
    private ActivityAssetAddedRTMProducer activityAssetAddedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        activityAssetAddedRTMProducer.buildActivityAssetAddedRTMConsumable(rtmClientContext, rootElementId, activityId, parentPathwayId);
        assertNotNull(activityAssetAddedRTMProducer.getEventConsumable());
    }
}
