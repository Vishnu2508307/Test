package com.smartsparrow.rtm.subscription.courseware.metainfochange;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CoursewareElementMetaInfoChangedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COURSEWARE_ELEMENT_META_INFO_CHANGED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "COURSEWARE_ELEMENT_META_INFO_CHANGED";
    }
}
