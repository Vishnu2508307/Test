package com.smartsparrow.rtm.message.handler.plugin;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.Exceptions;

public class DeletePluginMessageHandler implements MessageHandler<PluginGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeletePluginMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_DELETE = "workspace.plugin.delete";
    public static final String WORKSPACE_PLUGIN_DELETE_OK = "workspace.plugin.delete.ok";
    public static final String WORKSPACE_PLUGIN_DELETE_ERROR = "workspace.plugin.delete.error";

    private final PluginService pluginService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public DeletePluginMessageHandler(PluginService pluginService,
            Provider<AuthenticationContext> authenticationContextProvider) {
        this.pluginService = pluginService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(PluginGenericMessage message) throws RTMValidationException {
        if (message.getPluginId() == null) {
            throw new RTMValidationException("missing pluginId parameter", message.getId(), WORKSPACE_PLUGIN_DELETE_ERROR);
        }
        PluginSummary plugin = pluginService.fetchById(message.getPluginId()).block();
        if (plugin == null) {
            throw new RTMValidationException("plugin doesn't exist", message.getId(), WORKSPACE_PLUGIN_DELETE_ERROR);
        }
    }

    @Override
    public void handle(Session session, PluginGenericMessage message) throws WriteResponseException {
        UUID accountId = authenticationContextProvider.get().getAccount().getId();

        pluginService.deletePlugin(accountId, message.getPluginId())
                .doOnEach(log.reactiveErrorThrowable("error deleting plugin", throwable -> new HashMap<String, Object>() {
                    {
                        put("pluginId", message.getPluginId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .doOnComplete(() -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_DELETE_OK, message.getId());
                    Responses.writeReactive(session, basicResponseMessage);
                })
                .doOnError(ex -> {
                    Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_DELETE_ERROR,
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to delete plugin");
                })
                .subscribe();
    }
}
