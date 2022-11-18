package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.workspace.ProjectCreatedRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.CreateWorkspaceProjectMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.ProjectService;

import java.util.HashMap;

public class CreateWorkspaceProjectMessageHandler implements MessageHandler<CreateWorkspaceProjectMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateWorkspaceProjectMessageHandler.class);

    public static final String WORKSPACE_PROJECT_CREATE = "workspace.project.create";
    private static final String WORKSPACE_PROJECT_CREATE_OK = "workspace.project.create.ok";
    private static final String WORKSPACE_PROJECT_CREATE_ERROR = "workspace.project.create.error";

    private final ProjectService projectService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ProjectCreatedRTMProducer projectCreatedRTMProducer;

    @Inject
    public CreateWorkspaceProjectMessageHandler(final ProjectService projectService,
                                                final AuthenticationContextProvider authenticationContextProvider,
                                                final Provider<RTMClientContext> rtmClientContextProvider,
                                                final ProjectCreatedRTMProducer projectCreatedRTMProducer) {
        this.projectService = projectService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.projectCreatedRTMProducer = projectCreatedRTMProducer;
    }

    @Override
    public void validate(final CreateWorkspaceProjectMessage message) throws RTMValidationException {
        affirmArgument(message.getWorkspaceId() != null, "workspaceId is required");
        affirmArgument(message.getName() != null, "name is required");
    }

    @Override
    public void handle(final Session session, final CreateWorkspaceProjectMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        projectService.createProject(message.getName(), message.getConfig(), message.getWorkspaceId(), account.getId())
                .doOnEach(log.reactiveErrorThrowable("error creating the project", throwable -> new HashMap<String, Object>() {
                    {
                        put("workspaceId", message.getWorkspaceId());
                        put("accountId",account.getId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(project -> {
                    BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_PROJECT_CREATE_OK, message.getId())
                            .addField("project", project);

                    Responses.writeReactive(session, response);

                    projectCreatedRTMProducer.buildProjectCreatedRTMConsumable(rtmClientContext, message.getWorkspaceId(), project.getId())
                            .produce();
                }, ex -> {
                    log.debug("Project can't be created ", new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("workspaceId", message.getWorkspaceId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_CREATE_ERROR,
                            HttpStatus.UNPROCESSABLE_ENTITY_422, "error creating the project");
                });
    }
}
