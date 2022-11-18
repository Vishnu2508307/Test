package com.smartsparrow.rtm.message.handler.workspace;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.RevokeWorkspacePermissionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;

import java.util.HashMap;

public class RevokeWorkspacePermissionMessageHandler implements MessageHandler<RevokeWorkspacePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RevokeWorkspacePermissionMessageHandler.class);

    public static final String WORKSPACE_PERMISSION_REVOKE = "workspace.permission.revoke";
    private static final String WORKSPACE_PERMISSION_REVOKE_OK = "workspace.permission.revoke.ok";
    private static final String WORKSPACE_PERMISSION_REVOKE_ERROR = "workspace.permission.revoke.error";

    private final WorkspaceService workspaceService;

    @Inject
    public RevokeWorkspacePermissionMessageHandler(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public void validate(RevokeWorkspacePermissionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getWorkspaceId() != null, "workspaceId is required");

            if (message.getTeamId() != null && message.getAccountId() != null) {
                throw new IllegalArgumentException("too many arguments supplied. Either accountId or teamId is required");
            }

            if (message.getTeamId() == null && message.getAccountId() == null) {
                throw new IllegalArgumentException("either accountId or teamId is required");
            }

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_PERMISSION_REVOKE_ERROR);
        }
    }

    @Override
    public void handle(Session session, RevokeWorkspacePermissionMessage message) throws WriteResponseException {

        Flux<Void> permissionsDeleted;

        if (message.getTeamId() != null) {
            permissionsDeleted = workspaceService.deleteTeamPermission(message.getTeamId(), message.getWorkspaceId())
                    .doOnEach(log.reactiveErrorThrowable("error while deleting workspace permission for a team", throwable -> new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("teamId", message.getTeamId());
                        }
                    }))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
        } else {
             permissionsDeleted = workspaceService.deletePermissions(message.getAccountId(), message.getWorkspaceId())
                     .doOnEach(log.reactiveErrorThrowable("error while deleting workspace permission for an account", throwable -> new HashMap<String, Object>() {
                         {
                             put("workspaceId", message.getWorkspaceId());
                             put("accountId", message.getAccountId());
                         }
                     }))
                     .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
        }

        permissionsDeleted
                .subscribe(ignore->{
                    // nothing here, never executed
                }, ex ->{
                    log.jsonDebug("error while revoking permission for an account on workspace", new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("accountId", message.getAccountId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PERMISSION_REVOKE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while revoking permission");
                }, ()-> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PERMISSION_REVOKE_OK,
                        message.getId())));
    }
}
