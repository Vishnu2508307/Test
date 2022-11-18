package com.smartsparrow.rtm.subscription.plugin.granted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;

public class PluginPermissionGrantedRTMProducer extends AbstractProducer<PluginPermissionGrantedRTMConsumable> {

    private PluginPermissionGrantedRTMConsumable pluginPermissionGrantedRTMConsumable;

    @Inject
    public PluginPermissionGrantedRTMProducer() {
    }

    public PluginPermissionGrantedRTMProducer buildPluginPermissionGrantedRTMConsumable(RTMClientContext rtmClientContext, UUID pluginId, final UUID accountId, final UUID teamId) {
        this.pluginPermissionGrantedRTMConsumable = new PluginPermissionGrantedRTMConsumable(rtmClientContext, new PluginPermissionBroadcastMessage(pluginId, accountId, teamId));
        return this;
    }

    @Override
    public PluginPermissionGrantedRTMConsumable getEventConsumable() {
        return pluginPermissionGrantedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPermissionGrantedRTMProducer that = (PluginPermissionGrantedRTMProducer) o;
        return Objects.equals(pluginPermissionGrantedRTMConsumable, that.pluginPermissionGrantedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginPermissionGrantedRTMConsumable);
    }

    @Override
    public String toString() {
        return "PluginPermissionGrantedRTMProducer{" +
                "pluginPermissionGrantedRTMConsumable=" + pluginPermissionGrantedRTMConsumable +
                '}';
    }
}
