package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginAccountSummaryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import reactor.core.publisher.Flux;

public class PluginAccountSummaryMessageHandler implements MessageHandler<PluginAccountSummaryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginAccountSummaryMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_ACCOUNT_SUMMARY = "workspace.plugin.account.summary";
    public static final String WORKSPACE_PLUGIN_ACCOUNT_SUMMARY_OK = "workspace.plugin.account.summary.ok";
    public static final String WORKSPACE_PLUGIN_ACCOUNT_SUMMARY_ERROR = "workspace.plugin.account.summary.error";

    private final PluginService pluginService;
    private final AccountService accountService;

    @Inject
    public PluginAccountSummaryMessageHandler(
            PluginService pluginService,
            AccountService accountService) {
        this.pluginService = pluginService;
        this.accountService = accountService;
    }

    @Override
    public void handle(Session session, PluginAccountSummaryMessage message) throws WriteResponseException {

        Flux<PluginAccountCollaborator> collaboratorsFlux = pluginService.findAccountCollaborators(message.getPluginId());

        //total can't be null because even if collaboratorsFlux is empty count() return mono with '0'
        Long total = collaboratorsFlux.count().block();

        int limit = (message.getLimit() == null) ? total.intValue() : message.getLimit();

        collaboratorsFlux
                .take(limit)
                .flatMap(one -> accountService.getCollaboratorPayload(one.getAccountId(), one.getPermissionLevel()))
                .collectList()
                .doOnEach(log.reactiveErrorThrowable("Unable to fetch accounts summary list for plugin", throwable -> new HashMap<String, Object>() {
                    {
                        put("id", message.getId());
                        put("pluginId", message.getPluginId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(accountCollaboratorPayloads -> {
                    BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_PLUGIN_ACCOUNT_SUMMARY_OK,
                                                                             message.getId());
                    response.addField("collaborators", accountCollaboratorPayloads);
                    response.addField("total", total);
                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.jsonError("Unable to fetch accounts summary list for plugin ", new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("pluginId", message.getPluginId());
                            put("error", ex.getStackTrace());
                        }
                    }, ex);

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_ACCOUNT_SUMMARY_ERROR,
                                            HttpStatus.SC_BAD_REQUEST, "Unable to fetch accounts summary list for plugin");
                });
    }

    @Override
    public void validate(PluginAccountSummaryMessage message){
       affirmArgument(message.getPluginId() != null, "pluginId field is missing");
       affirmArgument(!(message.getLimit() != null && message.getLimit() < 0),  "limit should be >= 0");
    }

}