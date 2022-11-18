package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class ElementThemeCreateRTMEventDecoratorImplTest {
    private ElementThemeCreateRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new ElementThemeCreateRTMEventDecoratorImpl(new ElementThemeCreateRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ELEMENT_THEME_CREATE", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ELEMENT_THEME_CREATE", rtmEventDecorator.getLegacyName());
    }

}
