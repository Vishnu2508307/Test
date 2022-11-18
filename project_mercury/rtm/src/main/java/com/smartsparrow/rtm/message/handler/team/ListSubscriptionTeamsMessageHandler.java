package com.smartsparrow.rtm.message.handler.team;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.payload.TeamPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.ListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListSubscriptionTeamsMessageHandler implements MessageHandler<ListMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListSubscriptionTeamsMessageHandler.class);

    public static final String IAM_TEAM_SUBSCRIPTION_LIST = "iam.subscription.team.list";
    private static final String IAM_TEAM_SUBSCRIPTION_LIST_OK = "iam.subscription.team.list.ok";
    private static final String IAM_TEAM_SUBSCRIPTION_LIST_ERROR = "iam.subscription.team.list.error";

    private static final int COLLABORATOR_LIMIT = 3;

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final TeamService teamService;


    @Inject
    ListSubscriptionTeamsMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                        TeamService teamService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.teamService = teamService;
    }

    @Override
    public void validate(ListMessage message) throws RTMValidationException {
        Integer collaboratorLimit = message.getCollaboratorLimit();

        if (collaboratorLimit != null && collaboratorLimit < 0) {
            throw new RTMValidationException("collaboratorLimit should be a positive integer", message.getId(),
                    IAM_TEAM_SUBSCRIPTION_LIST_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_SUBSCRIPTION_LIST)
    @Override
    public void handle(Session session, ListMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        final int collaboratorLimit = getCollaboratorLimit(message.getCollaboratorLimit());

        teamService
                .findAllTeamsBySubscription(account.getSubscriptionId()) // Get all the teams for a subscription
                .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching team"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .flatMap(teamBySubscription -> teamService // Get the team payload for a team
                        .getTeamPayload(teamBySubscription.getTeamId(), collaboratorLimit))
                .collectList()
                .subscribe(teamPayloads -> {
                    emitSuccess(session, message, teamPayloads);
                }, ex -> {
                    log.jsonDebug("Exception while listing teams within the subscription", new HashMap<String, Object>(){
                        {
                            put("subscriptionId", account.getSubscriptionId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    emitError(session, message, account, ex);
                });
    }

    public void emitError(Session session, ListMessage message, Account account, Throwable ex) {
        Responses.errorReactive(
                session,
                message.getId(),
                IAM_TEAM_SUBSCRIPTION_LIST_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY,
                "Unable to list teams"
        );
    }

    public void emitSuccess(Session session, ListMessage message, List<TeamPayload> teamPayloads) {
        BasicResponseMessage reply = new BasicResponseMessage(IAM_TEAM_SUBSCRIPTION_LIST_OK, message.getId())
                .addField("teams", teamPayloads); // Return a collection of team payloads
        Responses.writeReactive(session, reply);
    }

    private int getCollaboratorLimit(Integer collaboratorLimit) {
        return collaboratorLimit != null ? collaboratorLimit : COLLABORATOR_LIMIT;
    }

}
