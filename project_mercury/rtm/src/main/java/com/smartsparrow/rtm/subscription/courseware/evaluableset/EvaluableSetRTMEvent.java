package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import com.smartsparrow.pubsub.data.RTMEvent;

public class EvaluableSetRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "EVALUABLE_SET";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "EVALUABLE_SET";
    }
}
