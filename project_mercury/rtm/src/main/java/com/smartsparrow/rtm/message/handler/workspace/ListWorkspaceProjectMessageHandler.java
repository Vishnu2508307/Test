package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.service.ProjectService;

import java.util.HashMap;

public class ListWorkspaceProjectMessageHandler implements MessageHandler<WorkspaceGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListWorkspaceProjectMessageHandler.class);

    public static final String WORKSPACE_PROJECT_LIST = "workspace.project.list";
    private static final String WORKSPACE_PROJECT_LIST_OK = "workspace.project.list.ok";
    private static final String WORKSPACE_PROJECT_LIST_ERROR = "workspace.project.list.error";

    private final ProjectService projectService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public ListWorkspaceProjectMessageHandler(final ProjectService projectService,
                                              final AuthenticationContextProvider authenticationContextProvider) {
        this.projectService = projectService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(final WorkspaceGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getWorkspaceId() != null, "workspaceId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = WORKSPACE_PROJECT_LIST)
    @Override
    public void handle(final Session session, final WorkspaceGenericMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        projectService.findAccountProjects(account.getId(), message.getWorkspaceId())
                .doOnEach(log.reactiveErrorThrowable("error listing projects", throwable -> new HashMap<String, Object>() {
                    {
                        put("workspaceId", message.getWorkspaceId());
                        put("accountId", account.getId());
                    }
                }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .collectList()
                .subscribe(projects -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_LIST_OK, message.getId())
                            .addField("projects", projects));
                }, ex -> {
                    log.jsonDebug("error listing projects for account in workspace ", new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), WORKSPACE_PROJECT_LIST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error listing projects");
                });
    }
}
