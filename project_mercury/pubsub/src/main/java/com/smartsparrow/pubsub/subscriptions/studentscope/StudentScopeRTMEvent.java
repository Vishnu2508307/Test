package com.smartsparrow.pubsub.subscriptions.studentscope;

import com.smartsparrow.pubsub.data.RTMEvent;

public class StudentScopeRTMEvent implements RTMEvent {

    @Override
    public String getName() {
        return "STUDENT_SCOPE";
    }

    @Override
    public Boolean equalsTo(final RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    /**
     * This method is used to support FE action
     * FIXME: this method will be removed when FE implements with getName() action
     */
    @Override
    public String getLegacyName() {
        return "STUDENT_SCOPE";
    }
}
