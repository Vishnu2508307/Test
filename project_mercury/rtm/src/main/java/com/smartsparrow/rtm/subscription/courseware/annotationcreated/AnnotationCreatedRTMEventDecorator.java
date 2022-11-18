package com.smartsparrow.rtm.subscription.courseware.annotationcreated;

import com.smartsparrow.pubsub.data.RTMEvent;

public abstract class AnnotationCreatedRTMEventDecorator implements RTMEvent {
    private final RTMEvent rtmEvent;

    public AnnotationCreatedRTMEventDecorator(final RTMEvent rtmEvent) {
        this.rtmEvent = rtmEvent;
    }

    @Override
    public String getName() {
        return rtmEvent.getName();
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return rtmEvent.equalsTo(rtmEvent);
    }

    @Override
    public String getLegacyName() {
        return rtmEvent.getLegacyName();
    }
}
