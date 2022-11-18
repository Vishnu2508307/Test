package com.smartsparrow.rtm.subscription.courseware.publication;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PublicationRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PUBLICATION_JOB";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "PUBLICATION_JOB";
    }
}
