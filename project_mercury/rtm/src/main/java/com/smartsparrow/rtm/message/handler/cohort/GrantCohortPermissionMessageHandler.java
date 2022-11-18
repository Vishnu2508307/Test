package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.GrantCohortPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.granted.CohortGrantedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;

public class GrantCohortPermissionMessageHandler implements MessageHandler<GrantCohortPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantCohortPermissionMessageHandler.class);

    public static final String WORKSPACE_COHORT_PERMISSION_GRANT = "workspace.cohort.permission.grant";
    private static final String WORKSPACE_COHORT_PERMISSION_GRANT_ERROR = "workspace.cohort.permission.grant.error";
    private static final String WORKSPACE_COHORT_PERMISSION_GRANT_OK = "workspace.cohort.permission.grant.ok";

    private final CohortService cohortService;
    private final AccountService accountService;
    private final TeamService teamService;
    private final CohortPermissionService cohortPermissionService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final CohortGrantedRTMProducer cohortGrantedRTMProducer;

    @Inject
    public GrantCohortPermissionMessageHandler(CohortService cohortService,
                                               AccountService accountService,
                                               TeamService teamService,
                                               CohortPermissionService cohortPermissionService,
                                               Provider<RTMClientContext> rtmClientContextProvider,
                                               CohortGrantedRTMProducer cohortGrantedRTMProducer) {
        this.cohortService = cohortService;
        this.accountService = accountService;
        this.teamService = teamService;
        this.cohortPermissionService = cohortPermissionService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.cohortGrantedRTMProducer = cohortGrantedRTMProducer;
    }

    @Override
    public void validate(GrantCohortPermissionMessage message) throws RTMValidationException {
        try {
            checkArgument((message.getAccountIds() == null || message.getAccountIds().isEmpty())
                    != (message.getTeamIds() == null || message.getTeamIds().isEmpty()), "either accountIds or teamIds is required");
            checkArgument(message.getCohortId() != null, "cohortId is required");
            checkArgument(message.getPermissionLevel() != null, "permissionLevel is required");

            checkArgument(cohortService.fetchCohortSummary(message.getCohortId()).block() != null,
                    String.format("cohort not found for id %s", message.getCohortId()));
            if (message.getAccountIds() != null) {
                message.getAccountIds().forEach(id ->
                        checkArgument(accountService.findById(id).blockLast() != null, String.format("account not found for id %s", id))
                );
            } else {
                message.getTeamIds().forEach(id ->
                        checkArgument(teamService.findTeam(id).block() != null, String.format("team not found for id %s", id))
                );
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_PERMISSION_GRANT_ERROR);
        }
    }

    @Override
    public void handle(Session session, GrantCohortPermissionMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Flux<Void> publisher;

        if (message.getAccountIds() != null) {
            publisher = cohortPermissionService.saveAccountPermissions(message.getAccountIds(), message.getCohortId(), message.getPermissionLevel());
        } else {
            publisher = cohortPermissionService.saveTeamPermissions(message.getTeamIds(), message.getCohortId(), message.getPermissionLevel());
        }

        publisher.subscribe(v -> {
            //do nothing on next
        }, ex -> {
            //on error
            log.jsonDebug("Unable to grant cohort permission", new HashMap<String, Object>(){
                {
                    put("message", message);
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_COHORT_PERMISSION_GRANT_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to grant cohort permission");
        }, () -> {
            //on complete
            BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_PERMISSION_GRANT_OK, message.getId())
                    .addField("cohortId", message.getCohortId())
                    .addField("permissionLevel", message.getPermissionLevel());

            if (message.getAccountIds() != null) {
                response.addField("accountIds", message.getAccountIds());
            } else {
                response.addField("teamIds", message.getTeamIds());
            }

            Responses.writeReactive(session, response);

            cohortGrantedRTMProducer.buildCohortGrantedRTMConsumable(rtmClientContext, message.getCohortId())
                    .produce();

        });
    }
}
