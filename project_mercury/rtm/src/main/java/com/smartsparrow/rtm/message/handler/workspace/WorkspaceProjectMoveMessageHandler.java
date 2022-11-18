package com.smartsparrow.rtm.message.handler.workspace;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceProjectMoveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.service.ProjectService;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class WorkspaceProjectMoveMessageHandler implements MessageHandler<WorkspaceProjectMoveMessage> {

    public static final String WORKSPACE_PROJECT_MOVE = "workspace.project.move";
    public static final String WORKSPACE_PROJECT_MOVE_OK = "workspace.project.move.ok";
    public static final String WORKSPACE_PROJECT_MOVE_ERROR = "workspace.project.move.error";

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceProjectMoveMessageHandler.class);
    private final ProjectService projectService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public WorkspaceProjectMoveMessageHandler(final ProjectService projectService,
                                              final AuthenticationContextProvider authenticationContextProvider) {
        this.projectService = projectService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(WorkspaceProjectMoveMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
        affirmArgument(message.getWorkspaceId() != null, "workspaceId is required");
    }

    @Override
    public void handle(Session session, WorkspaceProjectMoveMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        final UUID projectId = message.getProjectId();
        final UUID workspaceId = message.getWorkspaceId();

        projectService.moveProject(projectId, workspaceId, account.getId())
                .doOnEach(log.reactiveErrorThrowable("error moving project", throwable -> new HashMap<String, Object>() {
                    {
                        put("projectId", projectId);
                        put("workspaceId", workspaceId);
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(ignored -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_MOVE_OK, message.getId()));
                }, ex -> {
                    log.error("error! ", ex);
                    log.jsonDebug("error moving project", new HashMap<String, Object>() {
                        {
                            put("projectId", projectId);
                            put("workspaceId", workspaceId);
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_MOVE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error moving the project");
                });
    }
}
