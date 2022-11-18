package com.smartsparrow.rtm.subscription.courseware.assetadded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;

class AssetAddedRTMEventDecoratorImplTest {
    private AssetAddedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AssetAddedRTMEventDecoratorImpl(new AssetAddedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ASSET_ADDED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AssetAddedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ASSET_ADDED", rtmEventDecorator.getLegacyName());
    }
}
