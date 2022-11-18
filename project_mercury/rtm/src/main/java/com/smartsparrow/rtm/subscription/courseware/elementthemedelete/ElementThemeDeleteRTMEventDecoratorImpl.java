package com.smartsparrow.rtm.subscription.courseware.elementthemedelete;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.RTMEvent;

public class ElementThemeDeleteRTMEventDecoratorImpl extends ElementThemeDeleteRTMEventDecorator {

    public ElementThemeDeleteRTMEventDecoratorImpl(RTMEvent rtmEvent) {
        super(rtmEvent);
    }

    public String getName(CoursewareElementType elementType) {
        return elementType.toString() + "_" + super.getName();
    }

    public Boolean equalsTo(RTMEvent rtmEvent) {
        return super.equalsTo(rtmEvent);
    }

    public String getLegacyName() {
        return super.getLegacyName();
    }
}
