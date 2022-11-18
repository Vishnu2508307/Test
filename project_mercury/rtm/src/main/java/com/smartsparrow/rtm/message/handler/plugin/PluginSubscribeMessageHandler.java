package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription.PluginPermissionRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;

public class PluginSubscribeMessageHandler implements MessageHandler<PluginGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginSubscribeMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE = "workspace.plugin.permission.subscribe";
    static final String WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_OK = "workspace.plugin.permission.subscribe.ok";
    static final String WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_ERROR = "workspace.plugin.permission.subscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final PluginPermissionRTMSubscriptionFactory pluginPermissionRTMSubscriptionFactory;
    private final PluginService pluginService;

    @Inject
    public PluginSubscribeMessageHandler(final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                         final PluginPermissionRTMSubscription.PluginPermissionRTMSubscriptionFactory pluginPermissionRTMSubscriptionFactory,
                                         final PluginService pluginService) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.pluginPermissionRTMSubscriptionFactory = pluginPermissionRTMSubscriptionFactory;
        this.pluginService = pluginService;
    }

    @Override
    public void validate(PluginGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getPluginId() != null, "pluginId is required");
        PluginSummary summary = pluginService.fetchById(message.getPluginId()).block();
        affirmArgument(summary != null, String.format("plugin %s not found", message.getPluginId()));
    }

    @Override
    public void handle(Session session, PluginGenericMessage message) throws WriteResponseException {
        PluginPermissionRTMSubscription pluginPermissionRTMSubscription = pluginPermissionRTMSubscriptionFactory.create(message.getPluginId());

        rtmSubscriptionManagerProvider.get().add(pluginPermissionRTMSubscription)
                .subscribe(listenerId -> {
                           },
                           ex -> {
                               ex = Exceptions.unwrap(ex);
                               log.warn(ex.getMessage(), ex);
                               if (ex instanceof SubscriptionLimitExceeded || ex instanceof SubscriptionAlreadyExists) {
                                   throw new IllegalArgumentFault(ex.getMessage());
                               }
                               Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_ERROR, 400, ex.getMessage());
                           },
                           () -> {
                               BasicResponseMessage responseMessage = new BasicResponseMessage(
                                       WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_OK,
                                       message.getId());
                               responseMessage.addField("rtmSubscriptionId", pluginPermissionRTMSubscription.getId());
                               Responses.writeReactive(session, responseMessage);
                           }
                );
    }
}
