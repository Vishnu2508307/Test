package com.smartsparrow.rtm.message.handler.workspace;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.GrantProjectPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Flux;

public class GrantProjectPermissionMessageHandler implements MessageHandler<GrantProjectPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantProjectPermissionMessageHandler.class);

    public static final String WORKSPACE_PROJECT_PERMISSION_GRANT = "workspace.project.permission.grant";
    private static final String WORKSPACE_PROJECT_PERMISSION_GRANT_OK = "workspace.project.permission.grant.ok";
    private static final String WORKSPACE_PROJECT_PERMISSION_GRANT_ERROR = "workspace.project.permission.grant.error";

    private final TeamService teamService;
    private final AccountService accountService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public GrantProjectPermissionMessageHandler(final TeamService teamService,
                                                final AccountService accountService,
                                                final ProjectPermissionService projectPermissionService) {
        this.teamService = teamService;
        this.accountService = accountService;
        this.projectPermissionService = projectPermissionService;
    }

    @Override
    public void validate(final GrantProjectPermissionMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
        affirmArgument(message.getPermissionLevel() != null, "permissionLevel is required");

        try {
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
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_PROJECT_PERMISSION_GRANT_ERROR);
        }
    }

    @Override
    public void handle(final Session session, final GrantProjectPermissionMessage message) throws WriteResponseException {
        Flux<Void> savePermission;
        String field;

        final PermissionLevel permissionLevel = message.getPermissionLevel();
        final UUID projectId = message.getProjectId();

        Map<String, List<UUID>> fields = new HashMap<String, List<UUID>>(){
            {put("accountIds", message.getAccountIds());
                put("teamIds", message.getTeamIds());}
        };

        if (message.getTeamIds() != null) {
            field = "teamIds";
            savePermission = message.getTeamIds().stream()
                    .map(teamId -> projectPermissionService.saveTeamPermission(teamId, projectId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("Error while saving team permission"))
                            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT)))
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        } else {
            field = "accountIds";
            savePermission = message.getAccountIds().stream()
                    .map(accountId -> projectPermissionService.saveAccountPermission(accountId, projectId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("Error while saving account permission"))
                            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT)))
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        }

        savePermission
                .subscribe(success -> {
                    // do nothing here, never executed
                }, ex -> {
                    log.debug("error granting permission", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("accountIds", message.getAccountIds());
                            put("teamIds", message.getTeamIds());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_PERMISSION_GRANT_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "error granting permission");
                }, () -> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_PERMISSION_GRANT_OK, message.getId())
                        .addField(field, fields.get(field))
                        .addField("projectId", projectId)
                        .addField("permissionLevel", permissionLevel)));
    }
}
