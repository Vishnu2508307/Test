package com.smartsparrow.rtm.message.handler.team;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.DeleteTeamMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DeleteTeamMessageHandler implements MessageHandler<DeleteTeamMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteTeamMessageHandler.class);

    public static final String IAM_TEAM_DELETE = "iam.team.delete";
    public static final String IAM_TEAM_DELETE_OK = "iam.team.delete.ok";
    public static final String IAM_TEAM_DELETE_ERROR = "iam.team.delete.error";

    private final TeamService teamService;
    private final SubscriptionPermissionService subscriptionPermissionService;


    @Inject
    public DeleteTeamMessageHandler(TeamService teamService,
                                    SubscriptionPermissionService subscriptionPermissionService) {
        this.teamService = teamService;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public void validate(DeleteTeamMessage message) throws RTMValidationException {
        affirmArgument(message.getTeamId() != null, "missing teamId");
        affirmArgument(message.getSubscriptionId() != null, "missing subscriptionId");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_DELETE)
    public void handle(Session session, DeleteTeamMessage message) throws WriteResponseException {
        // Find all account ids associated with the team and delete all team accounts
        Flux<Void> accountFlux = teamService.findAllCollaboratorsForATeam(message.getTeamId())
                                .doOnEach(ReactiveTransaction.linkOnNext())
                                .flatMap(one -> teamService.deleteTeamAccount(message.getTeamId(), one.getAccountId())
                                        .doOnEach(ReactiveTransaction.linkOnNext()));


        // Find all subscription ids associated with the team and delete all team subscriptions
        Flux<UUID> subscriptionFlux = subscriptionPermissionService.findTeamSubscriptions(message.getTeamId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(one -> {
                    subscriptionPermissionService.deleteTeamPermission(message.getTeamId(), one);
                    teamService.deleteTeamSubscription(message.getTeamId(), one);
                    return Flux.just(one).doOnEach(ReactiveTransaction.linkOnNext());
                });

        // Delete team summary
        Flux<Void> teamFlux = teamService.deleteTeam(message.getTeamId()).doOnEach(ReactiveTransaction.linkOnNext());

        Flux.merge(accountFlux,
                   subscriptionFlux,
                   teamFlux)
            .doOnEach(log.reactiveErrorThrowable("Error occurred while deleting team"))
            // link each signal to the current transaction token
            .doOnEach(ReactiveTransaction.linkOnNext())
            // expire the transaction token on completion
            .doOnEach(ReactiveTransaction.expireOnComplete())
            // create a reactive context that enables all supported reactive monitoring
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(ignore -> {
                // nothing here, never executed
            }, ex -> {
                log.jsonDebug("Unable to delete team", new HashMap<String, Object>(){
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                Responses.errorReactive(session, message.getId(), IAM_TEAM_DELETE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                        "Unable to delete team");
            },
            ()-> Responses.writeReactive(session, new BasicResponseMessage(IAM_TEAM_DELETE_OK, message.getId())));
    }
}
