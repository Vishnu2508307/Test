package com.smartsparrow.rtm.message.handler.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.smartsparrow.rtm.subscription.plugin.revoked.PluginPermissionRevokedRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
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
import com.smartsparrow.rtm.message.recv.plugin.RevokePluginPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class RevokePluginPermissionMessageHandler implements MessageHandler<RevokePluginPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokePluginPermissionMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_PERMISSION_REVOKE = "workspace.plugin.permission.revoke";
    public static final String WORKSPACE_PLUGIN_PERMISSION_REVOKE_OK = "workspace.plugin.permission.revoke.ok";
    public static final String WORKSPACE_PLUGIN_PERMISSION_REVOKE_ERROR = "workspace.plugin.permission.revoke.error";

    private final AccountService accountService;
    private final TeamService teamService;
    private final PluginService pluginService;
    private final PluginPermissionService pluginPermissionService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PluginPermissionRevokedRTMProducer pluginPermissionRevokedRTMProducer;

    @Inject
    public RevokePluginPermissionMessageHandler(AccountService accountService,
                                                TeamService teamService,
                                                PluginService pluginService,
                                                PluginPermissionService pluginPermissionService,
                                                Provider<RTMClientContext> rtmClientContextProvider,
                                                PluginPermissionRevokedRTMProducer pluginPermissionRevokedRTMProducer) {
        this.accountService = accountService;
        this.teamService = teamService;
        this.pluginService = pluginService;
        this.pluginPermissionService = pluginPermissionService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pluginPermissionRevokedRTMProducer = pluginPermissionRevokedRTMProducer;
    }

    @Override
    public void handle(Session session, RevokePluginPermissionMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Flux<Void> flux;
        if (message.getAccountId() != null) {
            flux = pluginPermissionService.deleteAccountPermission(message.getAccountId(), message.getPluginId());
        } else {
            flux = pluginPermissionService.deleteTeamPermission(message.getTeamId(), message.getPluginId());
        }

        flux.doOnEach(log.reactiveErrorThrowable("Unable to revoke plugin permission", throwable -> new HashMap<String, Object>() {
                {
                    put("pluginId", message.getPluginId());
                    put("accountId", message.getAccountId());
                    put("teamId", message.getTeamId());
                }
            }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(t -> {
                //do nothing on next
                }, ex -> {
                    //on error
                    if (ex instanceof PluginPermissionPersistenceException) {
                        emitError(session, message.getId(), ex.getMessage());
                    } else {
                        emitError(session, message.getId(), "Unable to revoke plugin permission");
                    }
                }, () -> {
                    pluginPermissionRevokedRTMProducer.buildPluginPermissionRevokedRTMConsumable(rtmClientContext,
                                                                                                 message.getPluginId(),
                                                                                                 message.getAccountId(),
                                                                                                 message.getTeamId())
                            .produce();
                    emitSuccess(session, message.getId());
                });
    }

    /**
     * Checks that the required parameters are supplied
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when either accountId or pluginId are not supplied.
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void validate(RevokePluginPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument((message.getAccountId() == null) != (message.getTeamId() == null),
                    "either accountId or teamId is required");
            checkArgument(message.getPluginId() != null, "pluginId is required");

            PluginSummary summary = pluginService.fetchById(message.getPluginId()).block();
            checkArgument(summary != null, String.format("plugin %s not found", message.getPluginId()));

            if (message.getAccountId() != null) {
                Account account = accountService.findById(message.getAccountId()).blockLast();
                checkArgument(account != null, String.format("account %s not found", message.getAccountId()));
            } else {
                TeamSummary team = teamService.findTeam(message.getTeamId()).block();
                checkArgument(team != null, String.format("team %s not found", message.getTeamId()));
            }
        } catch (IllegalArgumentException iae) {
            throw new RTMValidationException(iae.getMessage(), message.getId(), WORKSPACE_PLUGIN_PERMISSION_REVOKE_ERROR);
        }
    }

    private void emitSuccess(Session session, String messageId) {
        Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PLUGIN_PERMISSION_REVOKE_OK, messageId));
    }

    private void emitError(Session session, String messageId, String message) {
        Responses.errorReactive(session, messageId, WORKSPACE_PLUGIN_PERMISSION_REVOKE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, message);
    }
}
