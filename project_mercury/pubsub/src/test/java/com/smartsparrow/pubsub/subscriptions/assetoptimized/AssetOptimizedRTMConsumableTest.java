package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class AssetOptimizedRTMConsumableTest {

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
    void create() {
        AssetOptimizedBroadcastMessage message = new AssetOptimizedBroadcastMessage(activityId,
                                                                                    elementId,
                                                                                    elementType,
                                                                                    assetId,
                                                                                    assetUrl);
        AssetOptimizedRTMConsumable consumable = new AssetOptimizedRTMConsumable(message);

        assertEquals(new AssetOptimizedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(String.format("author.activity/%s", activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
