package com.smartsparrow.rtm.message.handler.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginCollaborator;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginAccountSummaryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PluginCollaboratorSummaryMessageHandler implements MessageHandler<PluginAccountSummaryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginCollaboratorSummaryMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY = "workspace.plugin.collaborator.summary";
    public static final String WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK = "workspace.plugin.collaborator.summary.ok";
    public static final String WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_ERROR = "workspace.plugin.collaborator.summary.error";

    private final PluginService pluginService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public PluginCollaboratorSummaryMessageHandler(
            PluginService pluginService,
            AccountService accountService,
            TeamService teamService) {
        this.pluginService = pluginService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(PluginAccountSummaryMessage message) throws RTMValidationException {
        if (message.getPluginId() == null) {
            throwError(message, "pluginId field is missing");
        }

        if (message.getLimit() != null && message.getLimit() < 0) {
            throwError(message, String.format("limit '%s' should be >= 0", message.getLimit()));
        }
    }

    @Override
    public void handle(Session session, PluginAccountSummaryMessage message) throws WriteResponseException {

        Flux<PluginTeamCollaborator> teams = pluginService.findTeamCollaborators(message.getPluginId());
        Flux<PluginAccountCollaborator> accounts = pluginService.findAccountCollaborators(message.getPluginId());

        Flux<? extends PluginCollaborator> collaboratorsFlux = Flux.concat(teams, accounts);
        Mono<Long> total = collaboratorsFlux.count();

        if (message.getLimit() != null) {
            collaboratorsFlux = collaboratorsFlux.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> collaborators = collaboratorsFlux
                .flatMap(collaborator -> {
                    if (collaborator instanceof PluginTeamCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((PluginTeamCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel());
                    } else {
                        return accountService.getCollaboratorPayload(((PluginAccountCollaborator) collaborator).getAccountId(),
                                collaborator.getPermissionLevel());
                    }
                })
                //collect collaborators to two lists - one for teams, another for accounts
                .collect(() -> new HashMap<>(2), (map, payload) -> {
                    if (payload instanceof TeamCollaboratorPayload) {
                        map.computeIfAbsent("teams", x -> new ArrayList<>()).add(payload);
                    } else {
                        map.computeIfAbsent("accounts", x -> new ArrayList<>()).add(payload);
                    }
                });

        Mono.zip(collaborators, total).subscribe(tuple2 -> {
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK, message.getId())
                    .addField("collaborators", tuple2.getT1())
                    .addField("total", tuple2.getT2()));
        }, ex -> {
            log.jsonDebug("error while listing collaborators for plugin", new HashMap<String, Object>() {
                {
                    put("pluginId", message.getPluginId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators");
        });
    }

    private void throwError(PluginAccountSummaryMessage message, String errorMessage) throws RTMValidationException {
        throw new RTMValidationException(errorMessage, message.getId(), WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_ERROR);
    }

}
