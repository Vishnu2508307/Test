package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class AssetRemovedRTMEventDecoratorImplTest {
    private AssetRemovedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AssetRemovedRTMEventDecoratorImpl(new AssetRemovedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ASSET_REMOVED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ASSET_REMOVED", rtmEventDecorator.getLegacyName());
    }
}
