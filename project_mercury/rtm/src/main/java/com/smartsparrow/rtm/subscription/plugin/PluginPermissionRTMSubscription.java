package com.smartsparrow.rtm.subscription.plugin;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a plugin permission RTM subscription
 */
public class PluginPermissionRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = -42029858500904009L;

    public interface PluginPermissionRTMSubscriptionFactory {
        /**
         * Create a new instance of PluginPermissionRTMSubscription with a given pluginId
         *
         * @param pluginId the plugin id
         * @return the PluginPermissionRTMSubscription created instance
         */
        PluginPermissionRTMSubscription create(final UUID pluginId);
    }
    private UUID pluginId;

    /**
     * Provides the name of the PluginPermissionRTMSubscription
     *
     * @param pluginId the workspace id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID pluginId) {
        return String.format("courseware.plugin.permission/subscription/%s", pluginId);
    }

    @Inject
    public PluginPermissionRTMSubscription(@Assisted final UUID pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return PluginPermissionRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(pluginId);
    }

    @Override
    public String getBroadcastType() {
        return "workspace.plugin.permission.broadcast";
    }

    public UUID getPluginId() {
        return pluginId;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPermissionRTMSubscription that = (PluginPermissionRTMSubscription) o;
        return Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId);
    }

    @Override
    public String toString() {
        return "PluginPermissionRTMSubscription{" +
                "pluginId=" + pluginId +
                '}';
    }
}
