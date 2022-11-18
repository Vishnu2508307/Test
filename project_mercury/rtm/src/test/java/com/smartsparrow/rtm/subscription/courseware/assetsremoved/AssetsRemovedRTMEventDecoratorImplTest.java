package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class AssetsRemovedRTMEventDecoratorImplTest {
    private AssetsRemovedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AssetsRemovedRTMEventDecoratorImpl(new AssetsRemovedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ASSETS_REMOVED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ASSETS_REMOVED", rtmEventDecorator.getLegacyName());
    }
}
