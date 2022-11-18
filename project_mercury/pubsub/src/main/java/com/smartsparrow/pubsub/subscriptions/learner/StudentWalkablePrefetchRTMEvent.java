package com.smartsparrow.pubsub.subscriptions.learner;

import com.smartsparrow.pubsub.data.RTMEvent;

public class StudentWalkablePrefetchRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "STUDENT_WALKABLE_PREFETCH";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "STUDENT_WALKABLE_PREFETCH";
    }
}
