package com.smartsparrow.rtm.message.authorization;

import com.google.inject.Inject;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

/**
 * Authorization Predicate that allows operation only when plugin.allowSync config setting is true.
 */
public class AllowPluginSync implements AuthorizationPredicate<ReceivedMessage> {

    private final PluginConfig pluginConfig;

    @Inject
    public AllowPluginSync(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    /**
     * Returns the state of plugin.syncAllowed config setting.
     *
     * @param authenticationContext ignored
     * @param receivedMessage ignored
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {
        return pluginConfig.getAllowSync();
    }

    @Override
    public String getErrorMessage() {
        return "This feature is not enabled.";
    }
}
