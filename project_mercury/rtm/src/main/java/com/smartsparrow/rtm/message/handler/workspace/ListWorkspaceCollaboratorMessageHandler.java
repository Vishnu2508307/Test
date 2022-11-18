package com.smartsparrow.rtm.message.handler.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.payload.CollaboratorPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ListWorkspaceCollaboratorMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.data.WorkspaceAccountCollaborator;
import com.smartsparrow.workspace.data.WorkspaceCollaborator;
import com.smartsparrow.workspace.data.WorkspaceTeamCollaborator;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ListWorkspaceCollaboratorMessageHandler implements MessageHandler<ListWorkspaceCollaboratorMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListWorkspaceCollaboratorMessageHandler.class);

    public static final String WORKSPACE_COLLABORATOR_SUMMARY = "workspace.collaborator.summary";
    private static final String WORKSPACE_COLLABORATOR_SUMMARY_OK = "workspace.collaborator.summary.ok";
    static final String WORKSPACE_COLLABORATOR_SUMMARY_ERROR = "workspace.collaborator.summary.error";

    private final WorkspaceService workspaceService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public ListWorkspaceCollaboratorMessageHandler(WorkspaceService workspaceService,
                                                   AccountService accountService,
                                                   TeamService teamService) {
        this.workspaceService = workspaceService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(ListWorkspaceCollaboratorMessage message) throws RTMValidationException {
        if (message.getWorkspaceId() == null) {
            throw new RTMValidationException("missing workspaceId", message.getId(), WORKSPACE_COLLABORATOR_SUMMARY_ERROR);
        }
    }

    @Override
    public void handle(Session session, ListWorkspaceCollaboratorMessage message) throws WriteResponseException {
        Flux<WorkspaceTeamCollaborator> teams = workspaceService.fetchTeamCollaborators(message.getWorkspaceId());
        Flux<WorkspaceAccountCollaborator> accounts = workspaceService.fetchAccountCollaborators(message.getWorkspaceId());

        Flux<? extends WorkspaceCollaborator> collaboratorsFlux = Flux.concat(teams, accounts);

        Mono<Long> total = collaboratorsFlux.count();

        if (message.getLimit() != null) {
            collaboratorsFlux = collaboratorsFlux.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> collaborators = collaboratorsFlux
                .flatMap(collaborator ->  {
                    if (collaborator instanceof WorkspaceTeamCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((WorkspaceTeamCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel())
                                .doOnEach(log.reactiveErrorThrowable("error while getting team collaborator"))
                                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
                    } else {
                      return accountService.getCollaboratorPayload(((WorkspaceAccountCollaborator) collaborator).getAccountId(),
                              collaborator.getPermissionLevel())
                              .doOnEach(log.reactiveErrorThrowable("error while getting collaborator"))
                              .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
                    }
                })
                .collect(() -> new HashMap<>(2), (map, payload) -> {
                    if (payload instanceof TeamCollaboratorPayload) {
                        map.computeIfAbsent("teams", x -> new ArrayList<>()).add(payload);
                    } else {
                        map.computeIfAbsent("accounts", x -> new ArrayList<>()).add(payload);
                    }
                });

        Mono.zip(collaborators, total).subscribe(tuple2 -> {
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COLLABORATOR_SUMMARY_OK, message.getId())
                    .addField("collaborators", tuple2.getT1())
                    .addField("total", tuple2.getT2()));
        }, ex -> {
            log.jsonDebug("error while listing collaborators for plugin ", new HashMap<String, Object>() {
                {
                    put("workspaceId", message.getWorkspaceId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators");
        });
    }
}
