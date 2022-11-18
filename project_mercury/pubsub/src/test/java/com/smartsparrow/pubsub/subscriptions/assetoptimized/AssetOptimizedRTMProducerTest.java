package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class AssetOptimizedRTMProducerTest {

    @InjectMocks
    private AssetOptimizedRTMProducer assetOptimizedRTMProducer;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    private static final UUID assetId = UUIDs.timeBased();
    private static final String assetUrl = "http://test.com";

    @Mock
    private Object elementType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        assetOptimizedRTMProducer.buildAssetOptimizedRTMConsumable(activityId,
                                                                   elementId,
                                                                   elementType,
                                                                   assetId,
                                                                   assetUrl);
        assertNotNull(assetOptimizedRTMProducer.getEventConsumable());
    }

}
