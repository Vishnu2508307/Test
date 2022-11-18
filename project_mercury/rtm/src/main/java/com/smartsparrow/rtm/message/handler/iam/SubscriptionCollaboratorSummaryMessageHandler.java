package com.smartsparrow.rtm.message.handler.iam;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.data.SubscriptionAccountCollaborator;
import com.smartsparrow.iam.data.SubscriptionCollaborator;
import com.smartsparrow.iam.data.SubscriptionTeamCollaborator;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.SubscriptionCollaboratorSummayMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SubscriptionCollaboratorSummaryMessageHandler implements MessageHandler<SubscriptionCollaboratorSummayMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SubscriptionCollaboratorSummaryMessageHandler.class);

    public static final String IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY = "iam.subscription.collaborator.summary";
    private static final String IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY_OK = "iam.subscription.collaborator.summary.ok";
    private static final String IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY_ERROR = "iam.subscription.collaborator.summary.error";

    private final TeamService teamService;
    private final AccountService accountService;
    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public SubscriptionCollaboratorSummaryMessageHandler(TeamService teamService,
                                                         AccountService accountService,
                                                         SubscriptionPermissionService subscriptionPermissionService) {
        this.teamService = teamService;
        this.accountService = accountService;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public void validate(SubscriptionCollaboratorSummayMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getSubscriptionId() != null, "subscriptionId is required");
            if (message.getLimit() != null) {
                checkArgument(message.getLimit() >= 0,
                        String.format("limit `%s` should be >= 0", message.getLimit()));
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY)
    @Override
    public void handle(Session session, SubscriptionCollaboratorSummayMessage message) throws WriteResponseException {
        UUID subscriptionId = message.getSubscriptionId();

        Flux<SubscriptionTeamCollaborator> teams = subscriptionPermissionService.findTeamCollaborators(subscriptionId)
                .doOnEach(log.reactiveErrorThrowable("exception while finding team collaborators"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());

        Flux<SubscriptionAccountCollaborator> accounts = subscriptionPermissionService.findAccountCollaborators(subscriptionId)
                .doOnEach(log.reactiveErrorThrowable("exception while finding account collaborators"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());

        Flux<? extends SubscriptionCollaborator> collaboratorsFlux = Flux.concat(teams, accounts);
        Mono<Long> total = collaboratorsFlux.count();

        if (message.getLimit() != null) {
            collaboratorsFlux = collaboratorsFlux.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> collaborators = collaboratorsFlux
                .flatMap(collaborator -> {
                    if (collaborator instanceof SubscriptionTeamCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((SubscriptionTeamCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel());
                    } else {
                        return accountService.getCollaboratorPayload(((SubscriptionAccountCollaborator) collaborator).getAccountId(),
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
            Responses.writeReactive(session, new BasicResponseMessage(IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY_OK, message.getId())
                    .addField("collaborators", tuple2.getT1())
                    .addField("total", tuple2.getT2()));
        }, ex -> {
            log.jsonDebug("error while listing collaborators for subscription", new HashMap<String, Object>() {
                {
                    put("subscriptionId", message.getSubscriptionId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), IAM_SUBSCRIPTION_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators");
        });
    }
}
