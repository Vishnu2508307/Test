package com.smartsparrow.rtm.message.handler.workspace;

import static com.google.common.base.Preconditions.checkArgument;

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
import com.smartsparrow.rtm.message.recv.workspace.GrantWorkspacePermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;

public class GrantWorkspacePermissionMessageHandler implements MessageHandler<GrantWorkspacePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GrantWorkspacePermissionMessageHandler.class);

    public static final String WORKSPACE_PERMISSION_GRANT = "workspace.permission.grant";
    private static final String WORKSPACE_PERMISSION_GRANT_OK = "workspace.permission.grant.ok";
    private static final String WORKSPACE_PERMISSION_GRANT_ERROR = "workspace.permission.grant.error";

    private final WorkspaceService workspaceService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public GrantWorkspacePermissionMessageHandler(WorkspaceService workspaceService,
                                                  AccountService accountService,
                                                  TeamService teamService) {
        this.workspaceService = workspaceService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(GrantWorkspacePermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getWorkspaceId() != null, "workspaceId is required");
            checkArgument(message.getPermissionLevel() != null, "permissionLevel is required");
            checkArgument(workspaceService.fetchById(message.getWorkspaceId())
                    .block() != null, String.format("workspace %s not found", message.getWorkspaceId()));

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
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_PERMISSION_GRANT_ERROR);
        }
    }

    @Override
    public void handle(Session session, GrantWorkspacePermissionMessage message) throws WriteResponseException {

        Flux<Void> savePermission;
        String field;

        final PermissionLevel permissionLevel = message.getPermissionLevel();
        final UUID workspaceId = message.getWorkspaceId();

        Map<String, List<UUID>> fields = new HashMap<String, List<UUID>>(){
            {put("accountIds", message.getAccountIds());
                put("teamIds", message.getTeamIds());}
        };

        if (message.getTeamIds() != null) {
            field = "teamIds";
            savePermission = message.getTeamIds().stream()
                    .map(teamId-> workspaceService.saveTeamPermission(teamId, workspaceId, permissionLevel)
                            .doOnEach(log.reactiveErrorThrowable("Error while saving team permission"))
                            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT)))
                    .reduce((prev, next) -> Flux.merge(prev, next))
                    .orElse(Flux.empty());
        } else {
            field = "accountIds";
             savePermission = message.getAccountIds().stream()
                     .map(accountId -> workspaceService.savePermissions(accountId, workspaceId, permissionLevel)
                             .doOnEach(log.reactiveErrorThrowable("Error while saving account permission"))
                             .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT)))
                     .reduce((prev, next) -> Flux.merge(prev, next))
                     .orElse(Flux.empty());
        }

        savePermission
                .subscribe(success->{
                    // do nothing here, never executed
                }, ex->{
                    log.jsonDebug("error granting permission", new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("accountIds", message.getAccountIds());
                            put("teamIds", message.getTeamIds());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_PERMISSION_GRANT_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "error granting permission");
                }, ()-> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PERMISSION_GRANT_OK, message.getId())
                        .addField(field, fields.get(field))
                        .addField("workspaceId", message.getWorkspaceId())
                        .addField("permissionLevel", message.getPermissionLevel())));

    }
}
