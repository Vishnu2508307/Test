package com.smartsparrow.rtm.message.handler.plugin;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class ListPluginMessageHandler implements MessageHandler<EmptyReceivedMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListPluginMessageHandler.class);

    public final static String WORKSPACE_PLUGIN_LIST = "workspace.plugin.list";
    private final static String WORKSPACE_PLUGIN_LIST_OK = "workspace.plugin.list.ok";
    private final static String WORKSPACE_PLUGIN_LIST_ERROR = "workspace.plugin.list.error";

    private final PluginService pluginService;
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ListPluginMessageHandler(PluginService pluginService,
            Provider<AuthenticationContext> authenticationContextProvider) {
        this.pluginService = pluginService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    /**
     * Lists all the plugin the authenticated account has access to
     *
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when failing to write to the websocket
     */
    @Override
    public void handle(Session session, EmptyReceivedMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        if (account != null) {

            getPluginSummaryPayloads(account)
                    .doOnEach(log.reactiveErrorThrowable("error fetching plugins for account", throwable -> new HashMap<String, Object>() {
                        {
                            put("accountId", account.getId());
                        }
                    }))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .collectList()
                    .subscribe(payload -> {
                        emitSuccess(session, message, payload);
                    }, ex -> {
                        emitError(session, message, account, ex);
                    });

            return;
        }

        log.debug("could not fetch plugins for null account");

        Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_LIST_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error listing plugins");
    }

    /**
     * Fetch all the plugins the account has access to. Each plugin is then mapped to a {@link PluginSummaryPayload}
     * When an {@link AccountPayload} is not found the stream is not blocked and a default new object is created with the
     * known accountId set, this way the stream is able to proceed to the next emitted signal.
     *
     * @param account the account to fetch the plugins for
     * @return a flux of plugin summary
     */
    private Flux<PluginSummaryPayload> getPluginSummaryPayloads(Account account) {
        return pluginService.fetchPlugins(account.getId())
                .flatMap(pluginService::getPluginSummaryPayload);
    }

    private void emitError(Session session, EmptyReceivedMessage message, Account account, Throwable ex) {
        log.error("error while listing workspace plugins for account", new HashMap<String, Object>() {
            {
                put("accountId", account.getId());
                put("error", ex.getStackTrace());
            }
        });

        Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_LIST_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error listing plugins");
    }

    private void emitSuccess(Session session, EmptyReceivedMessage message, List<PluginSummaryPayload> payload) {
        Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PLUGIN_LIST_OK, message.getId())
                .addField("plugins", payload));
    }
}
