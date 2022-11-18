package com.smartsparrow.rtm.subscription.plugin.granted;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription;

public class PluginPermissionGrantedRTMConsumable extends AbstractRTMConsumable<PluginPermissionBroadcastMessage> {

    private static final long serialVersionUID = 3218588281161937273L;

    public PluginPermissionGrantedRTMConsumable(final RTMClientContext rtmClientContext,
                                                final PluginPermissionBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public String getName() {
        return String.format("courseware.plugin.permission/%s/%s", content.getPluginId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return PluginPermissionRTMSubscription.NAME(content.getPluginId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new PluginPermissionGrantedRTMEvent();
    }
}
