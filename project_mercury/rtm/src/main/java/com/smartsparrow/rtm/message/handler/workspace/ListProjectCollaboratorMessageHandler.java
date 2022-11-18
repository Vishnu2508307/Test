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
import com.smartsparrow.rtm.message.recv.workspace.ListProjectCollaboratorMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.data.ProjectAccountCollaborator;
import com.smartsparrow.workspace.data.ProjectCollaborator;
import com.smartsparrow.workspace.data.ProjectTeamCollaborator;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ListProjectCollaboratorMessageHandler implements MessageHandler<ListProjectCollaboratorMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListWorkspaceCollaboratorMessageHandler.class);

    public static final String WORKSPACE_PROJECT_COLLABORATOR_SUMMARY = "workspace.project.collaborator.summary";
    private static final String WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_OK = "workspace.project.collaborator.summary.ok";
    static final String WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR = "workspace.project.collaborator.summary.error";

    private final ProjectService projectService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public ListProjectCollaboratorMessageHandler(ProjectService projectService,
                                                 AccountService accountService,
                                                 TeamService teamService) {
        this.projectService = projectService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(ListProjectCollaboratorMessage message) throws RTMValidationException {
        if (message.getProjectId() == null) {
            throw new RTMValidationException("missing projectId", message.getId(), WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR);
        }
    }

    @Override
    public void handle(Session session, ListProjectCollaboratorMessage message) throws WriteResponseException {
        Flux<ProjectTeamCollaborator> teams = projectService.fetchTeamCollaborators(message.getProjectId());
        Flux<ProjectAccountCollaborator> accounts = projectService.fetchAccountCollaborators(message.getProjectId());

        Flux<? extends ProjectCollaborator> collaboratorsFlux = Flux.concat(teams, accounts);

        Mono<Long> total = collaboratorsFlux.count();

        if (message.getLimit() != null) {
            collaboratorsFlux = collaboratorsFlux.take(message.getLimit());
        }

        Mono<Map<String, List<CollaboratorPayload>>> collaborators = collaboratorsFlux
                .flatMap(collaborator -> {
                    if (collaborator instanceof ProjectTeamCollaborator) {
                        return teamService.getTeamCollaboratorPayload(((ProjectTeamCollaborator) collaborator).getTeamId(),
                                collaborator.getPermissionLevel())
                                .doOnEach(log.reactiveErrorThrowable("error while listing team collaborators"))
                                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
                    } else {
                        return accountService.getCollaboratorPayload(((ProjectAccountCollaborator) collaborator).getAccountId(),
                                collaborator.getPermissionLevel())
                                .doOnEach(log.reactiveErrorThrowable("error while listing collaborators"))
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

        Mono.zip(collaborators, total).subscribe(tuple2 -> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_OK, message.getId())
                .addField("collaborators", tuple2.getT1())
                .addField("total", tuple2.getT2())), ex -> {

            log.jsonDebug("error while listing collaborators for project", new HashMap<String, Object>() {
                {
                    put("projectId", message.getProjectId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_COLLABORATOR_SUMMARY_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while listing collaborators for project");
        });

    }
}