package com.smartsparrow.rtm.message.handler.team;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.RevokeTeamPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.util.HashMap;

public class RevokeTeamPermissionMessageHandler implements MessageHandler<RevokeTeamPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeTeamPermissionMessageHandler.class);

    public static final String IAM_TEAM_PERMISSION_REVOKE = "iam.team.permission.revoke";
    private static final String IAM_TEAM_PERMISSION_REVOKE_ERROR = "iam.team.permission.revoke.error";
    private static final String IAM_TEAM_PERMISSION_REVOKE_OK = "iam.team.permission.revoke.ok";

    private final TeamService teamService;

    @Inject
    public RevokeTeamPermissionMessageHandler(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public void validate(RevokeTeamPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getTeamId() != null, "teamId is required");
            checkArgument(message.getAccountIds() != null, "accountIds is required");

            if(message.getAccountIds() != null) {
                message.getAccountIds()
                       .forEach(accountId -> checkArgument(
                                teamService.fetchPermission(accountId, message.getTeamId()).block() != null,
                                "permission not found for account %s",accountId));
            }
        } catch (IllegalArgumentException e ) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_TEAM_PERMISSION_REVOKE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_PERMISSION_REVOKE)
    @Override
    public void handle(Session session, RevokeTeamPermissionMessage message) throws WriteResponseException {
        message
                .getAccountIds()
                .stream()
                .map(accountId-> teamService.deletePermission(accountId,message.getTeamId()))
                .reduce((prev,next) -> Flux.merge(prev,next))
                .orElse(Flux.empty())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while revoking team permissions"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(voided -> {
                    // nothing happens here
                }, ex -> {
                    log.jsonDebug("Failed to revoke team permission", new HashMap<String, Object>(){
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    emitError(session, message, ex);
                }, ()-> {
                    emitSuccess(session, message.getId());
                });
    }

    private void emitSuccess(Session session, String messageId) {
        Responses.writeReactive(session, new BasicResponseMessage(IAM_TEAM_PERMISSION_REVOKE_OK, messageId));
    }

    private void emitError(Session session, RevokeTeamPermissionMessage message, Throwable ex) {
        Responses.errorReactive(session, message.getId(), IAM_TEAM_PERMISSION_REVOKE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "failed to revoke permission");
    }


}
