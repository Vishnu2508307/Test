package com.smartsparrow.rtm.message.handler.team;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.CreateTeamMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class CreateTeamMessageHandler implements MessageHandler<CreateTeamMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateTeamMessageHandler.class);

    public static final String IAM_TEAM_CREATE = "iam.team.create";
    public static final String IAM_TEAM_CREATE_OK = "iam.team.create.ok";
    public static final String IAM_TEAM_CREATE_ERROR = "iam.team.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final TeamService teamService;
    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public CreateTeamMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                    TeamService teamService,
                                    SubscriptionPermissionService subscriptionPermissionService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.teamService = teamService;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public void validate(CreateTeamMessage message) throws RTMValidationException {
        if (Strings.isNullOrEmpty(message.getName())) {
            throw new RTMValidationException("Team name is required", message.getId(), IAM_TEAM_CREATE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_CREATE)
    public void handle(Session session, CreateTeamMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        // @formatter:off
        teamService.createTeam(account.getId(),
                                message.getName(),
                                message.getDescription(),
                                message.getThumbnail(),
                                account.getSubscriptionId())
                .flatMap(teamSummary -> {
                    return subscriptionPermissionService.saveTeamPermission(teamSummary.getId(), account.getSubscriptionId(), PermissionLevel.REVIEWER)
                            .singleOrEmpty()
                            .thenReturn(teamSummary);
                })
                .doOnEach(log.reactiveErrorThrowable("Error occurred while creating team"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(team -> emitResponse(session, message, team),
                ex -> {
                    log.jsonDebug("Unable to create team", new HashMap<String, Object>(){
                        {
                            put("accountId", account.getId());
                            put("message", message.toString());
                            put("subscriptionId", account.getSubscriptionId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    emitError(session, message);
                });
        // @formatter:on
    }

    private void emitResponse(Session session, CreateTeamMessage message, TeamSummary team) {
        BasicResponseMessage response = new BasicResponseMessage(IAM_TEAM_CREATE_OK, message.getId());
        response.addField("team", team);
        Responses.writeReactive(session, response);
    }

    private void emitError(Session session, CreateTeamMessage message) {
        Responses.errorReactive(session, message.getId(), IAM_TEAM_CREATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to create team");
    }
}
