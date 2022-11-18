package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.SyncPluginMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class SyncPluginMessageHandler implements MessageHandler<SyncPluginMessage> {

    public static final String WORKSPACE_PLUGIN_SYNC = "workspace.plugin.sync";
    private static final String WORKSPACE_PLUGIN_SYNC_OK = "workspace.plugin.sync.ok";
    private static final String WORKSPACE_PLUGIN_SYNC_ERROR = "workspace.plugin.sync.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final PluginService pluginService;

    @Inject
    public SyncPluginMessageHandler(AuthenticationContextProvider authenticationContextProvider,
                                    PluginService pluginService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.pluginService = pluginService;
    }

    /**
     * "Syncs" a plugin that is already published to S3 repo but that is missing fields in this environment's database.
     * This is to facilitate development and should not be enabled in production environments.
     * See {@link PluginConfig#getAllowSync()} for enabling the feature.
     *
     * @param session the websocket session
     * @param msg the newly arrived message
     * @throws WriteResponseException when the
     */
    @Override
    public void handle(Session session, SyncPluginMessage msg) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        try {
            pluginService.syncFromRepo(msg.getPluginId(), msg.getHash(), account)
                    .subscribe(pluginPayload -> {
                        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_SYNC_OK, msg.getId());
                        responseMessage.addField("plugin", pluginPayload);
                        Responses.writeReactive(session, responseMessage);
                    },
                    ex -> {
                        Responses.errorReactive(session, msg.getId(), WORKSPACE_PLUGIN_SYNC_ERROR, 400, ex.getMessage());
                    });
        } catch (S3BucketLoadFileException | IOException | IllegalArgumentException e) {
            Responses.error(session, msg.getId(), WORKSPACE_PLUGIN_SYNC_ERROR, 400, e.getMessage());
        }

    }

    @Override
    public void validate(SyncPluginMessage message) throws RTMValidationException {
            affirmNotNull(message.getPluginId(), "pluginId is required");
            affirmArgumentNotNullOrEmpty(message.getHash(), "hash is required");
    }


}
