package com.smartsparrow.rtm.subscription.plugin.granted;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PluginPermissionGrantedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PLUGIN_PERMISSION_GRANTED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    /**
     * This method is used to support FE action
     * FIXME: this method will be removed when FE implements with getName() action
     */
    @Override
    public String getLegacyName() {
        return "GRANTED";
    }
}
