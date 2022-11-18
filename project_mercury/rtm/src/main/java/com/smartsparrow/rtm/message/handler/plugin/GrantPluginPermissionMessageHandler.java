package com.smartsparrow.rtm.message.handler.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginPermissionPersistenceException;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.plugin.PluginPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.plugin.granted.PluginPermissionGrantedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;

public class GrantPluginPermissionMessageHandler implements MessageHandler<PluginPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantPluginPermissionMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_GRANT_PERMISSION = "workspace.plugin.permission.grant";
    static final String WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR = WORKSPACE_PLUGIN_GRANT_PERMISSION + ".error";
    static final String WORKSPACE_PLUGIN_GRANT_PERMISSION_OK = WORKSPACE_PLUGIN_GRANT_PERMISSION + ".ok";

    private final PluginService pluginService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final PluginPermissionService pluginPermissionService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PluginPermissionGrantedRTMProducer pluginPermissionGrantedRTMProducer;

    @Inject
    public GrantPluginPermissionMessageHandler(PluginService pluginService,
                                               AccountService accountService,
                                               TeamService teamService,
                                               PluginPermissionService pluginPermissionService,
                                               Provider<RTMClientContext> rtmClientContextProvider,
                                               PluginPermissionGrantedRTMProducer pluginPermissionGrantedRTMProducer) {
        this.pluginService = pluginService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.pluginPermissionService = pluginPermissionService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pluginPermissionGrantedRTMProducer = pluginPermissionGrantedRTMProducer;
    }

    @Override
    public void handle(Session session, PluginPermissionMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Flux<Void> flux;

        if (message.getAccountId() != null) {
            flux = pluginPermissionService.saveAccountPermission(message.getAccountId(), message.getPluginId(),
                    message.getPermissionLevel());
        } else {
            flux = pluginPermissionService.saveTeamPermission(message.getTeamId(), message.getPluginId(),
                    message.getPermissionLevel());
        }

        flux.subscribe(t -> {
            //do nothing on next
        }, ex -> {
            //on error
            if (ex instanceof PluginPermissionPersistenceException) {
                emitError(session, message.getId(), ex.getMessage());
            } else {
                log.jsonDebug("Unable to grant plugin permission ", new HashMap<String, Object>() {
                        {
                            put("permissionLevel", message.getPermissionLevel());
                            put("pluginId", message.getPluginId());
                            put("accountId", message.getAccountId());
                            put("teamId", message.getTeamId());
                            put("error", ex.getStackTrace());
                        }
                    });
                emitError(session, message.getId(), "Unable to grant plugin permission");
            }
        }, () -> {
            pluginPermissionGrantedRTMProducer.buildPluginPermissionGrantedRTMConsumable(rtmClientContext,
                                                                                         message.getPluginId(),
                                                                                         message.getAccountId(),
                                                                                         message.getTeamId())
                    .produce();
            emitSuccess(session, message);
        });
    }

    /**
     * Checks that the incoming message provides the required fields.
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the validation fails
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void validate(PluginPermissionMessage message) throws RTMValidationException {

        try {
            checkArgument((message.getAccountId() == null) != (message.getTeamId() == null),
                    "either accountId or teamId is required");
            checkArgument(message.getPluginId() != null, "pluginId is required");
            checkArgument(message.getPermissionLevel() != null, "permissionLevel is required");

            PluginSummary summary = pluginService.fetchById(message.getPluginId()).block();
            checkArgument(summary != null, String.format("plugin %s not found", message.getPluginId()));

            if (message.getAccountId() != null) {
                Account account = accountService.findById(message.getAccountId()).blockLast();
                checkArgument(account != null, String.format("account %s not found", message.getAccountId()));
            } else {
                TeamSummary team = teamService.findTeam(message.getTeamId()).block();
                checkArgument(team != null, String.format("team %s not found", message.getTeamId()));
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR);
        }
    }

    private void emitSuccess(Session session, PluginPermissionMessage message) {
        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_GRANT_PERMISSION_OK, message.getId());
        if (message.getAccountId() != null) {
            responseMessage.addField("accountId", message.getAccountId());
        } else {
            responseMessage.addField("teamId", message.getTeamId());
        }
        responseMessage.addField("pluginId", message.getPluginId());
        responseMessage.addField("permissionLevel", message.getPermissionLevel());
        Responses.writeReactive(session, responseMessage);
    }

    private void emitError(Session session, String inMessageId, String message) {
        Responses.errorReactive(session, inMessageId, WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, message);
    }
}
