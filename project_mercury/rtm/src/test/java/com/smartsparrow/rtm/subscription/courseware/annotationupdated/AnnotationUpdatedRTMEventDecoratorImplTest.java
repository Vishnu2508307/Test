package com.smartsparrow.rtm.subscription.courseware.annotationupdated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;

class AnnotationUpdatedRTMEventDecoratorImplTest {

    private AnnotationUpdatedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AnnotationUpdatedRTMEventDecoratorImpl(new AnnotationUpdatedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ANNOTATION_UPDATED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ANNOTATION_UPDATED", rtmEventDecorator.getLegacyName());
    }
}
