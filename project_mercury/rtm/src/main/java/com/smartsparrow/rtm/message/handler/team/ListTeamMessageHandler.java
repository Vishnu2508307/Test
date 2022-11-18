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

public class ListTeamMessageHandler implements MessageHandler<ListMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListTeamMessageHandler.class);

    public static final String IAM_TEAM_LIST = "iam.team.list";
    private static final String IAM_TEAM_LIST_ERROR = "iam.team.list.error";
    private static final String IAM_TEAM_LIST_OK = "iam.team.list.ok";

    private static final int DEFAULT_LIMIT = 3;

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final TeamService teamService;

    @Inject
    public ListTeamMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                  TeamService teamService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.teamService = teamService;
    }

    @Override
    public void validate(ListMessage message) throws RTMValidationException {
        Integer collaboratorsLimit = message.getCollaboratorLimit();

        if (collaboratorsLimit != null && collaboratorsLimit < 0) {
            throw new RTMValidationException("collaboratorsLimit should be a positive integer", message.getId(), IAM_TEAM_LIST_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_LIST)
    @Override
    public void handle(Session session, ListMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        final int limit = message.getCollaboratorLimit() != null ? message.getCollaboratorLimit() : DEFAULT_LIMIT;

        if (account != null) {
            teamService.findTeamsForAccount(account.getId())
                    .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching teams for account"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .flatMap(teamAccount -> teamService.getTeamPayload(teamAccount.getTeamId(), limit)).collectList()
                    .subscribe(teamPayloads -> {
                        emitSuccess(session, message, teamPayloads);
                    }, ex -> {
                        log.jsonDebug("Could not get teams list for account", new HashMap<String, Object>(){
                            {
                                put("accountId", account.getId());
                                put("error", ex.getStackTrace());
                            }
                        });
                        emitError(session, message, account, ex);
                    });

            return;
        }

        log.error("could not list teams for null account in message {}", message.toString());

        // emit an error for null account
        Responses.error(session, message.getId(), IAM_TEAM_LIST_ERROR, HttpStatus.SC_UNAUTHORIZED, "Error Listing teams");
    }

    public void emitError(Session session, ListMessage message, Account account, Throwable throwable) {
        Responses.errorReactive(session, message.getId(), IAM_TEAM_LIST_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not fetch teams");
    }

    private void emitSuccess(Session session, ListMessage message, List<TeamPayload> teamPayloads) {
        Responses.writeReactive(session, new BasicResponseMessage(IAM_TEAM_LIST_OK, message.getId())
        .addField("teams", teamPayloads));
    }
}
