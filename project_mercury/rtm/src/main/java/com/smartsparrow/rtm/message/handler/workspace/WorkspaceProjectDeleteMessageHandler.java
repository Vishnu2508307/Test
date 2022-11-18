package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.ProjectService;

import java.util.HashMap;

public class WorkspaceProjectDeleteMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceProjectDeleteMessageHandler.class);

    public static final String WORKSPACE_PROJECT_DELETE = "workspace.project.delete";
    private static final String WORKSPACE_PROJECT_DELETE_OK = "workspace.project.delete.ok";
    private static final String WORKSPACE_PROJECT_DELETE_ERROR = "workspace.project.delete.error";

    private final ProjectService projectService;

    @Inject
    public WorkspaceProjectDeleteMessageHandler(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void validate(final ProjectGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(final Session session, final ProjectGenericMessage message) throws WriteResponseException {
        projectService.deleteProject(message.getProjectId())
                .doOnEach(log.reactiveErrorThrowable("error deleting project", throwable -> new HashMap<String, Object>() {
                    {
                        put("projectId", message.getProjectId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(aVoid -> {
                    // nothing to do here
                }, ex -> {
                    log.debug("error deleting project", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("id", message.getId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_DELETE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting the project");
                }, () -> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_DELETE_OK, message.getId())));
    }
}
