package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;

class AnnotationDeletedRTMEventDecoratorImplTest {

    private AnnotationDeletedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AnnotationDeletedRTMEventDecoratorImpl(new AnnotationDeletedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ANNOTATION_DELETED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationDeletedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ANNOTATION_DELETED", rtmEventDecorator.getLegacyName());
    }
}
