package com.smartsparrow.rtm.subscription.plugin.revoked;

import com.smartsparrow.pubsub.data.RTMEvent;

public class PluginPermissionRevokedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "PLUGIN_PERMISSION_REVOKED";
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
        return "REVOKED";
    }
}
