package com.smartsparrow.rtm.subscription.plugin.revoked;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;

public class PluginPermissionRevokedRTMProducer extends AbstractProducer<PluginPermissionRevokedRTMConsumable> {

    private PluginPermissionRevokedRTMConsumable pluginPermissionRevokedRTMConsumable;

    @Inject
    public PluginPermissionRevokedRTMProducer() {
    }

    public PluginPermissionRevokedRTMProducer buildPluginPermissionRevokedRTMConsumable(RTMClientContext rtmClientContext, UUID pluginId, final UUID accountId, final UUID teamId) {
        this.pluginPermissionRevokedRTMConsumable = new PluginPermissionRevokedRTMConsumable(rtmClientContext, new PluginPermissionBroadcastMessage(pluginId, accountId, teamId));
        return this;
    }

    @Override
    public PluginPermissionRevokedRTMConsumable getEventConsumable() {
        return pluginPermissionRevokedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPermissionRevokedRTMProducer that = (PluginPermissionRevokedRTMProducer) o;
        return Objects.equals(pluginPermissionRevokedRTMConsumable, that.pluginPermissionRevokedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginPermissionRevokedRTMConsumable);
    }

    @Override
    public String toString() {
        return "PluginPermissionRevokedRTMProducer{" +
                "pluginPermissionRevokedRTMConsumable=" + pluginPermissionRevokedRTMConsumable +
                '}';
    }
}
