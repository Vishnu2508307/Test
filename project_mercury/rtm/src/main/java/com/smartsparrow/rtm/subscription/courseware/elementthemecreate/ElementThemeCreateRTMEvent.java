package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ElementThemeCreateRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ELEMENT_THEME_CREATE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ELEMENT_THEME_CREATE";
    }
}
