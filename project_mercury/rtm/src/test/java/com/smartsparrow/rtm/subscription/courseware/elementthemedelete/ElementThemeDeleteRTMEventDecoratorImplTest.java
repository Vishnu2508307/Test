package com.smartsparrow.rtm.subscription.courseware.elementthemedelete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class ElementThemeDeleteRTMEventDecoratorImplTest {
    private ElementThemeDeleteRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new ElementThemeDeleteRTMEventDecoratorImpl(new ElementThemeDeleteRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_ELEMENT_THEME_DELETE", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("ELEMENT_THEME_DELETE", rtmEventDecorator.getLegacyName());
    }

}
