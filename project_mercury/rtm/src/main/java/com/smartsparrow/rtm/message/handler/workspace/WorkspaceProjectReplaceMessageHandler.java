package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceProjectReplaceMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Mono;

public class WorkspaceProjectReplaceMessageHandler implements MessageHandler<WorkspaceProjectReplaceMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceProjectReplaceMessageHandler.class);

    public static final String WORKSPACE_PROJECT_REPLACE = "workspace.project.replace";
    private static final String WORKSPACE_PROJECT_REPLACE_OK = "workspace.project.replace.ok";
    private static final String WORKSPACE_PROJECT_REPLACE_ERROR = "workspace.project.replace.error";

    private final ProjectService projectService;

    @Inject
    public WorkspaceProjectReplaceMessageHandler(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public void validate(final WorkspaceProjectReplaceMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
        affirmArgument(message.getName() != null, "name is required");
        // config is optional
    }

    @Override
    public void handle(final Session session, final WorkspaceProjectReplaceMessage message) throws WriteResponseException {

        final UUID projectId = message.getProjectId();

        Mono<String> replacedName = projectService.replaceName(projectId, message.getName())
                .doOnEach(log.reactiveErrorThrowable("error updating the project", throwable -> new HashMap<String, Object>() {
                    {
                        put("projectId", projectId);
                        put("name", message.getName());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));

        Mono<String> replacedConfig = Mono.just("");

        // replace config only when it is supplied
        if (!Strings.isNullOrEmpty(message.getConfig())) {
            replacedConfig = projectService.replaceConfig(projectId, message.getConfig());
        }

        replacedName
                .then(replacedConfig)
                .subscribe(ignored -> {
                    BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_PROJECT_REPLACE_OK, message.getId())
                            .addField("projectId", projectId);

                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.jsonDebug("Project can't be updated", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("id", message.getId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_REPLACE_ERROR,
                            HttpStatus.UNPROCESSABLE_ENTITY_422, "error updating the project");
                });
    }
}
