package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.RTMEvent;

public class EvaluableSetRTMEventDecoratorImpl extends EvaluableSetRTMEventDecorator {

    public EvaluableSetRTMEventDecoratorImpl(RTMEvent rtmEvent) {
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
