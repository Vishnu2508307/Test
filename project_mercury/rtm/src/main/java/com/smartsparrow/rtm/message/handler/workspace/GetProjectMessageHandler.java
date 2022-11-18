package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
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

import reactor.core.Exceptions;

public class GetProjectMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetProjectMessageHandler.class);
    public static final String WORKSPACE_PROJECT_GET = "workspace.project.get";
    private static final String WORKSPACE_PROJECT_GET_OK = "workspace.project.get.ok";
    private static final String WORKSPACE_PROJECT_GET_ERROR = "workspace.project.get.error";

    private final ProjectService projectService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public GetProjectMessageHandler(final ProjectService projectService,
                                    final AuthenticationContextProvider authenticationContextProvider) {
        this.projectService = projectService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(ProjectGenericMessage message) throws RTMValidationException {
        affirmNotNull(message.getProjectId(), "projectId is required");
    }

    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        projectService.findPayloadById(message.getProjectId(), account.getId())
                .doOnEach(log.reactiveErrorThrowableIf("error fetching the project", throwable -> !(throwable instanceof NoSuchElementException)))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .single()
                .subscribe(project -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_GET_OK, message.getId())
                            .addField("project", project));
                }, ex -> {
                    Throwable throwable = Exceptions.unwrap(ex);

                    log.jsonDebug("error fetching the project", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    if (throwable instanceof NoSuchElementException) {
                        Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_GET_ERROR,
                                HttpStatus.SC_NOT_FOUND, String.format("project with id %s not found", message.getProjectId()));
                    } else {
                        Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_GET_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching the project");
                    }
                });
    }
}
