package com.smartsparrow.rtm.message.handler.workspace;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ChangeWorkspaceMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.workspace.service.WorkspaceService;

import java.util.HashMap;

public class ChangeWorkspaceMessageHandler implements MessageHandler<ChangeWorkspaceMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ChangeWorkspaceMessageHandler.class);

    public static final String WORKSPACE_CHANGE = "workspace.change";
    public static final String WORKSPACE_CHANGE_OK = "workspace.change.ok";
    public static final String WORKSPACE_CHANGE_ERROR = "workspace.change.error";

    private final WorkspaceService workspaceService;

    @Inject
    public ChangeWorkspaceMessageHandler(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public void validate(ChangeWorkspaceMessage message) throws RTMValidationException {
        if (message.getWorkspaceId() == null) {
            throw new RTMValidationException("missing workspaceId", message.getId(), WORKSPACE_CHANGE_ERROR);
        }
        if (Strings.isNullOrEmpty(message.getName())) {
            throw new RTMValidationException("missing name", message.getId(), WORKSPACE_CHANGE_ERROR);
        }
    }

    @Override
    public void handle(Session session, ChangeWorkspaceMessage message) throws WriteResponseException {

        workspaceService.updateWorkspace(message.getWorkspaceId(), message.getName(), message.getDescription())
                .doOnEach(log.reactiveErrorThrowable("Unable to update workspace", throwable -> new HashMap<String, Object>() {
                    {
                        put("workspaceId", message.getWorkspaceId());
                        put("id", message.getId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(workspace -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_CHANGE_OK, message.getId());
                    basicResponseMessage.addField("workspace", workspace);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    log.jsonDebug("Unable to update workspace", new HashMap<String, Object>() {
                        {
                            put("workspaceId", message.getWorkspaceId());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_CHANGE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to update workspace");
                });
    }
}
