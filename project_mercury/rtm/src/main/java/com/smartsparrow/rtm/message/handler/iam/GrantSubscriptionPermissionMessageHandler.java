package com.smartsparrow.rtm.message.handler.iam;

import static com.google.common.base.Preconditions.checkArgument;

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

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.iam.service.SubscriptionService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.GrantSubscriptionPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class GrantSubscriptionPermissionMessageHandler implements MessageHandler<GrantSubscriptionPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantSubscriptionPermissionMessageHandler.class);

    public static final String IAM_SUBSCRIPTION_PERMISSION_GRANT = "iam.subscription.permission.grant";
    private static final String IAM_SUBSCRIPTION_PERMISSION_GRANT_OK = "iam.subscription.permission.grant.ok";
    private static final String IAM_SUBSCRIPTION_PERMISSION_GRANT_ERROR = "iam.subscription.permission.grant.error";

    private final SubscriptionPermissionService subscriptionPermissionService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final SubscriptionService subscriptionService;

    @Inject
    public GrantSubscriptionPermissionMessageHandler(SubscriptionPermissionService subscriptionPermissionService,
                                                     AccountService accountService,
                                                     TeamService teamService,
                                                     SubscriptionService subscriptionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.subscriptionService = subscriptionService;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(GrantSubscriptionPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getSubscriptionId() != null, "subscriptionId is required");
            checkArgument(message.getPermissionLevel() != null, "permissionLevel is required");
            checkArgument(subscriptionService.find(message.getSubscriptionId())
                    .singleOrEmpty()
                    .block() != null, String.format("subscription %s not found", message.getSubscriptionId()));

            if (message.getTeamIds() != null && message.getAccountIds() != null) {
                throw new IllegalArgumentException("too many arguments supplied. Either accountIds or teamIds is required");
            }

            if (message.getTeamIds() == null && message.getAccountIds() == null) {
                throw new IllegalArgumentException("either accountIds or teamIds is required");
            }

            if (message.getTeamIds() != null) {
                checkArgument(!message.getTeamIds().isEmpty(), "at least 1 element in teamIds is required");

                message.getTeamIds().forEach(teamId -> {
                    checkArgument(teamService.findTeam(teamId).block() != null,
                            String.format("team %s not found", teamId));
                });
            }

            if (message.getAccountIds() != null) {
                checkArgument(!message.getAccountIds().isEmpty(), "at least 1 element in accountIds is required");

                message.getAccountIds().forEach(accountId -> {
                    checkArgument(accountService.findById(accountId)
                            .blockLast() != null, String.format("account %s not found", accountId));
                });
            }

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_SUBSCRIPTION_PERMISSION_GRANT_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_SUBSCRIPTION_PERMISSION_GRANT)
    @Override
    public void handle(Session session, GrantSubscriptionPermissionMessage message) throws WriteResponseException {
        Flux<Void> savePermission;
        String field;

        final PermissionLevel permissionLevel = message.getPermissionLevel();
        final UUID subscriptionId = message.getSubscriptionId();

        Map<String, List<UUID>> fields = new HashMap<String, List<UUID>>(){
            {put("accountIds", message.getAccountIds());
                put("teamIds", message.getTeamIds());}
        };

        if (message.getTeamIds() != null) {
            // saveAccountPermission = saveTeamsPemission
            field = "teamIds";
            savePermission = message.getTeamIds().stream()
                    .map(teamId-> subscriptionPermissionService.saveTeamPermission(teamId, subscriptionId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("exception while fetching the account"))
                    )
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        } else {
            field = "accountIds";
            savePermission = message.getAccountIds().stream()
                    .map(accountId -> subscriptionPermissionService.saveAccountPermission(accountId, subscriptionId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("exception while fetching the account"))
                    )
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        }

        savePermission
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(success->{
                    // do nothing here, never executed
                }, ex->{
                    Throwable throwable = Exceptions.unwrap(ex);
                    log.jsonDebug("error while granting the permission", new HashMap<String, Object>(){
                        {
                            put("field", field);
                            put("fieldIds", fields.get(field).toString());
                            put("subscriptionId", message.getSubscriptionId());
                            put("error", throwable.getStackTrace());
                        }
                });
                    Responses.errorReactive(session, message.getId(), IAM_SUBSCRIPTION_PERMISSION_GRANT_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "error granting permission");
                }, ()-> Responses.writeReactive(session, new BasicResponseMessage(IAM_SUBSCRIPTION_PERMISSION_GRANT_OK, message.getId())
                        .addField(field, fields.get(field))
                        .addField("subscriptionId", message.getSubscriptionId())
                        .addField("permissionLevel", message.getPermissionLevel())));
    }

}
