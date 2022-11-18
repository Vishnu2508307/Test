package com.smartsparrow.rtm.subscription.courseware.annotationupdated;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AnnotationUpdatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ANNOTATION_UPDATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ANNOTATION_UPDATED";
    }
}
