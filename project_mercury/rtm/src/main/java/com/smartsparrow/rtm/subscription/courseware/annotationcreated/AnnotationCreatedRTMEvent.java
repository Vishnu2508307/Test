package com.smartsparrow.rtm.subscription.courseware.annotationcreated;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AnnotationCreatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ANNOTATION_CREATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ANNOTATION_CREATED";
    }
}
