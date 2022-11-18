package com.smartsparrow.workspace.subscription;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ProjectRTMEvent implements RTMEvent {

    @Override
    public String getName() {
        return "PROJECT_EVENT";
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
        return "PROJECT_EVENT";
    }
}
