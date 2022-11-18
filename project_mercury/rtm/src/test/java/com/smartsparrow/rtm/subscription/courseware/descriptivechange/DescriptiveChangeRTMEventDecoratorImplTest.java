package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class DescriptiveChangeRTMEventDecoratorImplTest {
    private DescriptiveChangeRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new DescriptiveChangeRTMEventDecoratorImpl(new DescriptiveChangeRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_DESCRIPTIVE_CHANGE", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("DESCRIPTIVE_CHANGE", rtmEventDecorator.getLegacyName());
    }


}
