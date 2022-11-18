package com.smartsparrow.rtm.message.handler.team;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.TeamPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class GrantTeamPermissionMessageHandler implements MessageHandler<TeamPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantTeamPermissionMessageHandler.class);

    public static final String IAM_TEAM_PERMISSION_GRANT = "iam.team.permission.grant";
    private static final String IAM_TEAM_PERMISSION_GRANT_ERROR = "iam.team.permission.grant.error";
    private static final String IAM_TEAM_PERMISSION_GRANT_OK = "iam.team.permission.grant.ok";

    private final TeamService teamService;
    private final AccountService accountService;

    @Inject
    public GrantTeamPermissionMessageHandler(TeamService teamService,
                                             AccountService accountService) {
        this.teamService = teamService;
        this.accountService = accountService;
    }

    @Override
    public void validate(TeamPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getAccountIds() != null, "accountIds is required");

            if(message.getAccountIds() != null){
                checkArgument(!message.getAccountIds().isEmpty(),
                        "at least 1 element in accountIds is required");

                message.getAccountIds().forEach(accountId -> checkArgument(
                        accountService.findById(accountId).blockLast() != null,
                        String.format("account %s not found",accountId))
                );
            }
            checkArgument(message.getPermissionLevel() != null, "permissionLevel is required");
            checkArgument(message.getTeamId() != null, "teamId is required");
            checkArgument(teamService.findTeam(message.getTeamId())
                    .block() != null, String.format("team %s not found", message.getTeamId()));

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_TEAM_PERMISSION_GRANT_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_PERMISSION_GRANT)
    @Override
    public void handle(Session session, final TeamPermissionMessage message) throws WriteResponseException {

        message
                .getAccountIds()
                .stream()
                .map(accountId -> teamService.savePermission(accountId,message.getTeamId(),message.getPermissionLevel()))
                .reduce((prev,next) -> Flux.merge(prev,next))
                .orElse(Flux.empty())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while granting team permissions"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(voided -> {
                    // nothing happens here
                }, ex -> {
                    log.jsonDebug("Failed to grant team permission", new HashMap<String, Object>(){
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    emitError(session, message, ex);
                }, () -> {
                    emitSuccess(session, message);
                });
    }

    private void emitError(Session session, TeamPermissionMessage message, Throwable ex) {
        Responses.errorReactive(session, message.getId(), IAM_TEAM_PERMISSION_GRANT_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "failed to grant permission");
    }

    private void emitSuccess(Session session, TeamPermissionMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(IAM_TEAM_PERMISSION_GRANT_OK, message.getId())
                .addField("accountIds", message.getAccountIds())
                .addField("teamId", message.getTeamId())
                .addField("permissionLevel", message.getPermissionLevel()));
    }
}
