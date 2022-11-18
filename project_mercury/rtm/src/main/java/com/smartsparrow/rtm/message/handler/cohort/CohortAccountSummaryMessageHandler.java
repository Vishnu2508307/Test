package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.cohort.data.AccountCohortCollaborator;
import com.smartsparrow.cohort.data.CohortCollaborator;
import com.smartsparrow.cohort.data.TeamCohortCollaborator;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortCollaboratorSummaryMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CohortAccountSummaryMessageHandler implements MessageHandler<CohortCollaboratorSummaryMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortAccountSummaryMessageHandler.class);

    public static final String WORKSPACE_COHORT_COLLABORATOR_SUMMARY = "workspace.cohort.collaborator.summary";
    private static final String WORKSPACE_COHORT_COLLABORATOR_SUMMARY_ERROR = "workspace.cohort.collaborator.summary.error";
    private static final String WORKSPACE_COHORT_COLLABORATOR_SUMMARY_OK = "workspace.cohort.collaborator.summary.ok";

    private final CohortService cohortService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public CohortAccountSummaryMessageHandler(CohortService cohortService,
                                              AccountService accountService,
                                              TeamService teamService) {
        this.cohortService = cohortService;
        this.accountService = accountService;
        this.teamService = teamService;
    }


    @Override
    public void handle(Session session, CohortCollaboratorSummaryMessage message) throws WriteResponseException {
        Flux<AccountCohortCollaborator> accounts = cohortService.fetchAccountCollaborators(message.getCohortId());
        Flux<TeamCohortCollaborator> teams = cohortService.fetchTeamCollaborators(message.getCohortId());

        Flux<? extends CohortCollaborator> collaborators = Flux.concat(teams, accounts);

        Mono<Long> total = collaborators.count();

        if (message.getLimit() != null) {
            collaborators = collaborators.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> result = collaborators
                .flatMap(collaborator -> {
                    if (collaborator instanceof TeamCohortCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((TeamCohortCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel());
                    } else {
                        return accountService.getCollaboratorPayload(((AccountCohortCollaborator) collaborator).getAccountId(),
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

        Mono.zip(result, total).subscribe(tuple2 -> {
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COHORT_COLLABORATOR_SUMMARY_OK, message.getId())
                    .addField("collaborators", tuple2.getT1())
                    .addField("total", tuple2.getT2()));
        }, ex -> {
            log.jsonDebug("error while listing collaborators for cohort", new HashMap<String, Object>(){
                {
                    put("cohortId", message.getCohortId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_COHORT_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators");
        });
    }

    @Override
    public void validate(CohortCollaboratorSummaryMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");
            checkArgument(cohortService.fetchCohortSummary(message.getCohortId()).block() != null,
                    String.format("cohort not found for id %s", message.getCohortId()));
            if (message.getLimit() != null) {
                checkArgument(message.getLimit() >= 0, "limit should be >= 0");
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_COLLABORATOR_SUMMARY_ERROR);
        }
    }
}
