package com.smartsparrow.rtm.message.handler.workspace;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.DeleteWorkspaceMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.service.WorkspaceService;

public class DeleteWorkspaceMessageHandler implements MessageHandler<DeleteWorkspaceMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteWorkspaceMessageHandler.class);

    public static final String WORKSPACE_DELETE = "workspace.delete";
    public static final String WORKSPACE_DELETE_OK = "workspace.delete.ok";
    public static final String WORKSPACE_DELETE_ERROR = "workspace.delete.error";

    private final WorkspaceService workspaceService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public DeleteWorkspaceMessageHandler(WorkspaceService workspaceService,
                                         AuthenticationContextProvider authenticationContextProvider) {
        this.workspaceService = workspaceService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(DeleteWorkspaceMessage message) throws RTMValidationException {
        if (message.getWorkspaceId() == null) {
            throw new RTMValidationException("missing workspaceId", message.getId(), WORKSPACE_DELETE_ERROR);
        }
        if (Strings.isNullOrEmpty(message.getName())) {
            throw new RTMValidationException("missing name", message.getId(), WORKSPACE_DELETE_ERROR);
        }
        if (message.getSubscriptionId() == null) {
            throw new RTMValidationException("missing subscriptionId", message.getId(), WORKSPACE_DELETE_ERROR);
        }
    }

    @Override
    public void handle(Session session, DeleteWorkspaceMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        workspaceService.deleteWorkspace(message.getWorkspaceId(), message.getName(), account.getId(), message.getSubscriptionId())
                .doOnEach(log.reactiveErrorThrowable("Unable to delete workspace", throwable -> new HashMap<String, Object>() {
                    {
                        put("workspaceId", message.getWorkspaceId());
                        put("id", message.getId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(ignore->{
                    // nothing here, never executed
                }, ex -> {
                    log.jsonDebug("Unable to delete workspace", new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_DELETE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to delete workspace");
                },
               ()-> Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_DELETE_OK, message.getId())));
    }
}
