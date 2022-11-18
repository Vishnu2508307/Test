package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMEvent;

class EvaluableSetRTMEventDecoratorImplTest {
    private EvaluableSetRTMEventDecoratorImpl rtmEventDecorator;

    @BeforeEach
    void setUp() {
        rtmEventDecorator = new EvaluableSetRTMEventDecoratorImpl(new EvaluableSetRTMEvent());
    }

    @Test
    void getName() {
        assertEquals("ACTIVITY_EVALUABLE_SET", rtmEventDecorator.getName(CoursewareElementType.ACTIVITY));
    }

    @Test
    void equalsTo() {
        assertTrue(rtmEventDecorator.equalsTo(new AnnotationUpdatedRTMEvent()));
    }

    @Test
    void getLegacyName() {
        assertEquals("EVALUABLE_SET", rtmEventDecorator.getLegacyName());
    }

}
