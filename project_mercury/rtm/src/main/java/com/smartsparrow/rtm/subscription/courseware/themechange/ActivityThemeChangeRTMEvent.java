package com.smartsparrow.rtm.subscription.courseware.themechange;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityThemeChangeRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_THEME_CHANGE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "THEME_CHANGE";
    }
}
