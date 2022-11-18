package com.smartsparrow.rtm.message.handler.team;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.team.TeamAccountSummaryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TeamAccountSummaryMessageHandler implements MessageHandler<TeamAccountSummaryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(TeamAccountSummaryMessageHandler.class);

    public static final String IAM_TEAM_ACCOUNT_SUMMARY = "iam.team.account.summary";
    private static final String IAM_TEAM_ACCOUNT_SUMMARY_ERROR = "iam.team.account.summary.error";
    private static final String IAM_TEAM_ACCOUNT_SUMMARY_OK = "iam.team.account.summary.ok";

    private final TeamService teamService;
    private final AccountService accountService;

    @Inject
    public TeamAccountSummaryMessageHandler(TeamService teamService,
                                            AccountService accountService) {
        this.teamService = teamService;
        this.accountService = accountService;
    }

    @Override
    public void validate(TeamAccountSummaryMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getTeamId() != null, "teamId is required");
            checkArgument(teamService.findTeam(message.getTeamId()).block() != null,
                    String.format("team %s not found", message.getTeamId()));

            if (message.getLimit() != null) {
                checkArgument(message.getLimit() >= 0, "limit should be a positive integer");
            }
        } catch (IllegalArgumentException e ) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_TEAM_ACCOUNT_SUMMARY_ERROR);
        }
    }

    /**
     * Fetches all the collaborators of a team. Only the <code>limit</code> are mapped to a collaborator payload
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when failing to write the response
     */
    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_TEAM_ACCOUNT_SUMMARY)
    public void handle(Session session, TeamAccountSummaryMessage message) throws WriteResponseException {

        // find the collaborators
        Flux<AccountTeamCollaborator> collaboratorFlux = teamService
                .findAllCollaboratorsForATeam(message.getTeamId())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching collaborators for a team"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());

        // work out the limit to take
        Mono<Long> limitMono = getLimit(message, collaboratorFlux);

        // get a list of mapped collaborators payload
        Mono<List<AccountCollaboratorPayload>> collaborators = Mono.zip(limitMono, Mono.just(collaboratorFlux))
                .map(tuple-> getCollaboratorsPayload(tuple.getT2(), tuple.getT1()))
                .flatMap(one->one);

        // zip the streams and subscribe
        Mono.zip(collaboratorFlux.count(), collaborators)
                .subscribe(tuple-> {
                    emitSuccess(session, message, tuple.getT2(), tuple.getT1());
                }, ex -> {
                    log.jsonDebug("Error while fetching collaborators", new HashMap<String, Object>(){
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    emitError(session, message, ex);
                });

    }

    public void emitSuccess(Session session, TeamAccountSummaryMessage message, List<AccountCollaboratorPayload> collaborators,
                            Long total) {
        Responses.writeReactive(session, new BasicResponseMessage(IAM_TEAM_ACCOUNT_SUMMARY_OK, message.getId())
                .addField("collaborators", collaborators)
                .addField("total", total));
    }

    private void emitError(Session session, TeamAccountSummaryMessage message, Throwable ex) {
        String errorMessage = "Error while fetching collaborators";
        int code = HttpStatus.SC_UNPROCESSABLE_ENTITY;
        Responses.errorReactive(session, message.getId(), IAM_TEAM_ACCOUNT_SUMMARY_ERROR, code, errorMessage);
    }

    /**
     * Either returns the <code>limit</code> from the message, or if not supplied gets the limit from the flux count.
     * This limit will be then used to take the entries in the collaboratorsFlux
     *
     * @param message the message that could contain the limit
     * @param collaboratorFlux all the team collaborator wrapped in a flux
     * @return the desired limit
     */
    private Mono<Long> getLimit(TeamAccountSummaryMessage message, Flux<AccountTeamCollaborator> collaboratorFlux) {
        if (message.getLimit() != null) {
            return Mono.just(Long.valueOf(message.getLimit()));
        }

        return collaboratorFlux.count();
    }

    /**
     * Build a collaborator payload for each accountTeamCollaborator in the supplied {@link Flux} argument. If building
     * the collaborator completes with an error the entry is skipped and the listing continues.
     * @param collaboratorsFlux all the team collaborators
     * @param limit the amount of acocunt payload summary to map
     * @return a mono list of collaborators
     */
    @Trace(async = true)
    private Mono<List<AccountCollaboratorPayload>> getCollaboratorsPayload(Flux<AccountTeamCollaborator> collaboratorsFlux,
                                                                           long limit) {
        return collaboratorsFlux
                .take(limit)
                .flatMap(one-> accountService
                        .getCollaboratorPayload(one.getAccountId(), one.getPermissionLevel())
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .onErrorResume(throwable -> {
                            log.error("error while fetching the collaborator payload for {} {}",
                                    one.toString(), throwable.getMessage());
                            return Mono.empty();
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .collectList();
    }
}
