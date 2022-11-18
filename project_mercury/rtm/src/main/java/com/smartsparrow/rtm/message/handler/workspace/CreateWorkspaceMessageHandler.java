package com.smartsparrow.rtm.message.handler.workspace;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.CreateWorkspaceMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.WorkspaceService;

public class CreateWorkspaceMessageHandler implements MessageHandler<CreateWorkspaceMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateWorkspaceMessageHandler.class);

    public static final String WORKSPACE_CREATE = "workspace.create";
    public static final String WORKSPACE_CREATE_OK = "workspace.create.ok";
    public static final String WORKSPACE_CREATE_ERROR = "workspace.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final WorkspaceService workspaceService;

    @Inject
    public CreateWorkspaceMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                         WorkspaceService workspaceService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.workspaceService = workspaceService;
    }

    @Override
    public void validate(CreateWorkspaceMessage message) throws RTMValidationException {
        if (Strings.isNullOrEmpty(message.getName())) {
            throw new RTMValidationException("missing name", message.getId(), WORKSPACE_CREATE_ERROR);
        }
    }

    @Override
    public void handle(Session session, CreateWorkspaceMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        UUID accountId = account.getId();
        UUID subscriptionId = account.getSubscriptionId();

        workspaceService.createWorkspace(subscriptionId, accountId, message.getName(), message.getDescription())
                .doOnEach(log.reactiveErrorThrowable("error creating workspace", throwable -> new HashMap<String, Object>() {
                    {
                        put("subscriptionId", subscriptionId);
                        put("accountId", accountId);
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(workspace -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_CREATE_OK, message.getId());
                    basicResponseMessage.addField("workspace", workspace);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    log.jsonDebug("Unable to create workspace", new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("type", message.getType());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_CREATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to create workspace");
                });
    }
}
