package com.smartsparrow.rtm.subscription.courseware.annotationcreated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;

class AnnotationCreatedRTMEventDecoratorImplTest {

    private AnnotationCreatedRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new AnnotationCreatedRTMEventDecoratorImpl(new AnnotationCreatedRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ANNOTATION_CREATED", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationCreatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ANNOTATION_CREATED", rtmEventDecorator.getLegacyName());
    }
}
