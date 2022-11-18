package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

import com.smartsparrow.pubsub.data.RTMEvent;

public class AnnotationDeletedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ANNOTATION_DELETED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ANNOTATION_DELETED";
    }
}
