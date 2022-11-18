package com.smartsparrow.rtm.message.handler.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.CreatePluginMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Mono;

public class CreatePluginMessageHandler implements MessageHandler<CreatePluginMessage> {

    public static final String WORKSPACE_PLUGIN_CREATE = "workspace.plugin.create";
    private static final String WORKSPACE_PLUGIN_CREATE_OK = "workspace.plugin.create.ok";
    private static final String WORKSPACE_PLUGIN_CREATE_ERROR = "workspace.plugin.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final PluginService pluginService;

    @Inject
    public CreatePluginMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                      PluginService pluginService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.pluginService = pluginService;
    }

    /**
     * Creates a new plugin summary. A success message is emitted on the socket including a {@link PluginSummaryPayload}.
     *
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when the
     */
    @Override
    public void handle(Session session, CreatePluginMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        Mono<PluginSummary> pluginSummaryMono;

        if(message.getPluginId() != null ) {
            pluginSummaryMono = pluginService.createPluginSummary(message.getName(),
                                                        message.getPluginType(),
                                                        message.getPluginId(),
                                                        message.getPublishMode(),
                                                        account);
        }else  {
            pluginSummaryMono = pluginService.createPluginSummary(message.getName(),
                                                                  message.getPluginType(),
                                                                  message.getPublishMode(),
                                                                  account);
        }
        PluginSummaryPayload payload = pluginSummaryMono.flatMap(pluginService::getPluginSummaryPayload).block();

        emitSuccess(session, message.getId(), payload);
    }

    /**
     * Checks that the {@link CreatePluginMessage#getName()} parameter is supplied and valid.
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when {@link CreatePluginMessage#getName()} is not supplied or empty
     */
    @Override
    public void validate(CreatePluginMessage message) throws RTMValidationException {
        try {
            checkArgument(!Strings.isNullOrEmpty(message.getName()), "name is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_PLUGIN_CREATE_ERROR);
        }
    }

    /**
     * Emit a success error message.
     *
     * @param session     the session to write to
     * @param inMessageId the id from the received message
     * @param payload     the created plugin summary DTO
     * @throws WriteResponseException when unable to write the response
     */
    private void emitSuccess(Session session, String inMessageId, PluginSummaryPayload payload)
            throws WriteResponseException {
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_CREATE_OK, inMessageId);
        basicResponseMessage.addField("plugin", payload);
        Responses.write(session, basicResponseMessage);
    }
}
