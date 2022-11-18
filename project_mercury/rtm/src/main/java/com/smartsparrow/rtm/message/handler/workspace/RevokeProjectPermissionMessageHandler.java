package com.smartsparrow.rtm.message.handler.workspace;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.RevokeProjectPermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class RevokeProjectPermissionMessageHandler implements MessageHandler<RevokeProjectPermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeProjectPermissionMessageHandler.class);

    public static final String WORKSPACE_PROJECT_PERMISSION_REVOKE = "workspace.project.permission.revoke";
    private static final String WORKSPACE_PROJECT_PERMISSION_REVOKE_OK = "workspace.project.permission.revoke.ok";
    private static final String WORKSPACE_PROJECT_PERMISSION_REVOKE_ERROR = "workspace.project.permission.revoke.error";

    private final ProjectPermissionService projectPermissionService;
    private final AccountService accountService;
    private final TeamService teamService;

    @Inject
    public RevokeProjectPermissionMessageHandler(final ProjectPermissionService projectPermissionService,
                                                 final AccountService accountService,
                                                 final TeamService teamService) {
        this.projectPermissionService = projectPermissionService;
        this.accountService = accountService;
        this.teamService = teamService;
    }

    @Override
    public void validate(final RevokeProjectPermissionMessage message) throws RTMValidationException {
        affirmArgument((message.getAccountId() == null) != (message.getTeamId() == null),
                "either accountId or teamId is required");
        affirmArgument(message.getProjectId() != null, "projectId is required");

        try {
            if (message.getAccountId() != null) {
                Account account = accountService.findById(message.getAccountId()).blockLast();
                checkArgument(account != null, String.format("account %s not found", message.getAccountId()));
            } else {
                TeamSummary team = teamService.findTeam(message.getTeamId()).block();
                checkArgument(team != null, String.format("team %s not found", message.getTeamId()));
            }
        } catch (IllegalArgumentException iae) {
            throw new RTMValidationException(iae.getMessage(), message.getId(), WORKSPACE_PROJECT_PERMISSION_REVOKE_ERROR);
        }
    }

    @Override
    public void handle(final Session session, final RevokeProjectPermissionMessage message) throws WriteResponseException {
        Flux<Void> flux;
        if (message.getAccountId() != null) {
            flux = projectPermissionService.deleteAccountPermission(message.getAccountId(), message.getProjectId())
                    .doOnEach(log.reactiveErrorThrowable("error while deleting project permission for an account", throwable -> new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("accountId", message.getAccountId());
                        }
                    }))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
        } else {
            flux = projectPermissionService.deleteTeamPermission(message.getTeamId(), message.getProjectId())
                    .doOnEach(log.reactiveErrorThrowable("error while deleting project permission for a team", throwable -> new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("teamId", message.getTeamId());
                        }
                    }))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));;
        }

        flux.subscribe(t -> {
            //do nothing on next
        }, ex -> {
            log.jsonDebug("could not revoke permission to project", new HashMap<String, Object>() {
                {
                    put("projectId", message.getProjectId());
                    put("error", ex.getStackTrace());
                }
            });
            Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_PERMISSION_REVOKE_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "Could not revoke permission");
        }, () -> {
            //on complete
            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_PERMISSION_REVOKE_OK, message.getId()));
        });
    }
}
