package com.smartsparrow.rtm.subscription.plugin.revoked;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;

import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class PluginPermissionRevokedRTMConsumer implements RTMConsumer<PluginPermissionRevokedRTMConsumable> {

    MercuryLogger logger = MercuryLoggerFactory.getLogger(PluginPermissionRevokedRTMConsumer.class);

    private final AccountService accountService;
    private final TeamService teamService;
    private final PluginService pluginService;

    @Inject
    public PluginPermissionRevokedRTMConsumer(final AccountService accountService,
                                              final TeamService teamService,
                                              final PluginService pluginService) {
        this.accountService = accountService;
        this.teamService = teamService;
        this.pluginService = pluginService;
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new PluginPermissionRevokedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the pluginId, teamId, accountId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param pluginPermissionRevokedRTMConsumable the produced consumable
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void accept(final RTMClient rtmClient,
                       final PluginPermissionRevokedRTMConsumable pluginPermissionRevokedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = pluginPermissionRevokedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            PluginPermissionBroadcastMessage message = pluginPermissionRevokedRTMConsumable.getContent();

            final String broadcastType = pluginPermissionRevokedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = pluginPermissionRevokedRTMConsumable.getSubscriptionId();
            final UUID pluginId = message.getPluginId();
            final UUID accountId = message.getAccountId();
            final UUID teamId = message.getTeamId();

            Mono<CollaboratorPayload> collaborator;

            if (accountId != null) {
                Mono<PluginAccountCollaborator> accountCollaborator = pluginService.findAccountCollaborator(pluginId, accountId)
                        .defaultIfEmpty(new PluginAccountCollaborator()); // collaborator can be not found if it's revoked changes

                collaborator = accountCollaborator
                        .flatMap(coll -> accountService.getCollaboratorPayload(accountId, coll.getPermissionLevel()));
            } else if (teamId != null) {
                Mono<PluginTeamCollaborator> teamCollaborator = pluginService.findTeamCollaborator(pluginId, teamId)
                        .defaultIfEmpty(new PluginTeamCollaborator()); // collaborator can be not found if it's revoked changes

                collaborator =
                        teamCollaborator.flatMap(coll -> teamService.getTeamCollaboratorPayload(teamId, coll.getPermissionLevel()));
            } else {
                logger.error("both accountId and teamId can not be nullable: " + message);
                return;
            }

            collaborator
                    .subscribe(collaboratorPayload -> {
                        BasicResponseMessage responseMessage = new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                .addField("pluginId", pluginId)
                                // TODO remove next line when FE supported
                                .addField("action", getRTMEvent().getLegacyName())
                                .addField("rtmEvent", getRTMEvent().getName())
                                .addField("collaborator", collaboratorPayload);
                        Responses.writeReactive(rtmClient.getSession(), responseMessage);
                    }, ex -> {
                        ex = Exceptions.unwrap(ex);
                        String errorMessage =
                                String.format("Error fetching collaborator with account/team %s/%s for plugin %s",
                                              accountId, teamId, pluginId);
                        logger.jsonError(errorMessage, new HashMap<String, Object>() {
                            {put("clientId", rtmClient.getRtmClientContext().getClientId());}
                            {put("accountId", accountId);}
                            {put("teamId", teamId);}
                            {put("pluginId", pluginId);}
                        }, ex);
                        Responses.errorReactive(rtmClient.getSession(), subscriptionId.toString(), broadcastType+".error",
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while broadcasting the changes");
                    });
        }
    }
}
