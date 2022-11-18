package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;
import com.smartsparrow.rtm.subscription.courseware.assetadded.AssetAddedRTMEvent;
import com.smartsparrow.rtm.subscription.courseware.assetadded.AssetAddedRTMEventDecoratorImpl;

class AssetOptimizedRTMEventDecoratorImplTest {
    private AssetOptimizedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AssetOptimizedRTMEventDecoratorImpl(new AssetOptimizedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ASSET_OPTIMIZED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
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
