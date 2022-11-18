package com.smartsparrow.rtm.subscription.courseware.elementthemedelete;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ElementThemeDeleteRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ELEMENT_THEME_DELETE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ELEMENT_THEME_DELETE";
    }
}
