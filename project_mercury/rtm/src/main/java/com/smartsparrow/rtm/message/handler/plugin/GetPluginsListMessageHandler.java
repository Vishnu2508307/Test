package com.smartsparrow.rtm.message.handler.plugin;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.ListPluginsMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;

public class GetPluginsListMessageHandler implements MessageHandler<ListPluginsMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetPluginsListMessageHandler.class);

    public static final String AUTHOR_PLUGIN_LIST = "author.plugin.list";
    static final String AUTHOR_PLUGIN_LIST_OK = "author.plugin.list.ok";

    private final PluginService pluginService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public GetPluginsListMessageHandler(PluginService pluginService,
                                        Provider<AuthenticationContext> authenticationContextProvider) {
        this.pluginService = pluginService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    /**
     * Fetch all the published plugin summaries that the account has access to
     *
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when error occurs while writing message on the socket
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PLUGIN_LIST)
    @Override
    public void handle(Session session, ListPluginsMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ACCOUNT_ID.getValue(), account.getId().toString(), log);

        List<PluginSummaryPayload> all = pluginService.fetchPublishedPlugins(account.getId(), message.getPluginType())
                .flatMap(pluginService::getPluginSummaryPayload)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("error fetching published plugins", throwable -> new HashMap<String, Object>() {
                    {
                        put("pluginType", message.getPluginType().toString());
                        put("accountId", account.getId());
                    }
                }))
                .collectList()
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                }).block();

        //if plugin filters is supplied, then filter plugin summary payloads based on list of plugin filters, else return summaries as it is.
        List<PluginSummaryPayload> filteredPluginSummaryPayloads = pluginService.filterPluginSummaryPayloads(all,
                                                                                                    message.getPluginFilters());

        emitSuccess(session, filteredPluginSummaryPayloads, message.getId());
    }

    private void emitSuccess(Session session, List<PluginSummaryPayload> pluginSummaries, String messageId) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(AUTHOR_PLUGIN_LIST_OK, messageId);
        responseMessage.addField("plugins", pluginSummaries);
        Responses.write(session, responseMessage);
    }
}
