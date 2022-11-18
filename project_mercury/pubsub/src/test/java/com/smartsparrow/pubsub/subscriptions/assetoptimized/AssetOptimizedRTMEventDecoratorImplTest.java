package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetOptimizedRTMEventDecoratorImplTest {
    private AssetOptimizedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ASSET_OPTIMIZED", rtmEventDecorator.getName("ACTIVITY"));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AssetOptimizedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ASSET_OPTIMIZED", rtmEventDecorator.getLegacyName());
    }
}
