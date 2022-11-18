package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;


public class PluginAccountListMessageHandler implements MessageHandler<PluginGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginAccountListMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_ACCOUNT_LIST = "workspace.plugin.account.list";
    private static final String WORKSPACE_PLUGIN_ACCOUNT_LIST_OK = "workspace.plugin.account.list.ok";
    private static final String WORKSPACE_PLUGIN_ACCOUNT_LIST_ERROR = "workspace.plugin.account.list.error";

    private final PluginService pluginService;
    private final AccountService accountService;

    @Inject
    public PluginAccountListMessageHandler(PluginService pluginService,
                                           AccountService accountService) {
        this.pluginService = pluginService;
        this.accountService = accountService;
    }

    @Override
    public void handle(Session session, PluginGenericMessage message) throws WriteResponseException {
        pluginService.findAccountCollaborators(message.getPluginId())
                .flatMap(one -> accountService.getCollaboratorPayload(one.getAccountId(), one.getPermissionLevel()))
                .collectList()
                .doOnEach(log.reactiveErrorThrowable(
                        "Unable to fetch accounts list for plugin ",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("id", message.getId());
                                put("pluginId", message.getPluginId());
                            }
                        }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(accountCollaboratorPayloads -> {
                    BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_PLUGIN_ACCOUNT_LIST_OK,
                                                                             message.getId());
                    response.addField("collaborators", accountCollaboratorPayloads);
                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.debug("can't fetch accounts list for plugin ", new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("pluginId", message.getPluginId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session,
                                            message.getId(),
                                            WORKSPACE_PLUGIN_ACCOUNT_LIST_ERROR,
                                            HttpStatus.SC_BAD_REQUEST,
                                            "Unable to fetch accounts list for plugin ");

                });
    }

    @Override
    public void validate(PluginGenericMessage message){
        affirmArgument(message.getPluginId() != null, "Unable to fetch accounts list for plugin: pluginId field is missing");
    }
}
