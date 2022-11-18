package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class WorkspaceSubscribeMessageHandler implements MessageHandler<WorkspaceGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceSubscribeMessageHandler.class);

    public static final String WORKSPACE_SUBSCRIBE = "workspace.subscribe";
    public static final String WORKSPACE_SUBSCRIBE_OK = "workspace.subscribe.ok";
    public static final String WORKSPACE_SUBSCRIBE_ERROR = "workspace.subscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final WorkspaceRTMSubscriptionFactory workspaceRTMSubscriptionFactory;

    @Inject
    public WorkspaceSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                            final WorkspaceRTMSubscriptionFactory workspaceRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.workspaceRTMSubscriptionFactory = workspaceRTMSubscriptionFactory;
    }

    @Override
    public void validate(WorkspaceGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getWorkspaceId() != null, "workspaceId is required");
    }

    @Override
    public void handle(Session session, WorkspaceGenericMessage message) throws WriteResponseException {

        WorkspaceRTMSubscription workspaceRTMSubscription = workspaceRTMSubscriptionFactory.create(message.getWorkspaceId());

        rtmSubscriptionManagerProvider.get().add(workspaceRTMSubscription)
                .subscribe(listenerId -> {
                    BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_SUBSCRIBE_OK, message.getId());
                    response.addField("rtmSubscriptionId", workspaceRTMSubscription.getId());
                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.jsonError(ex.getMessage(), new HashMap<>(), ex);
                    Responses.errorReactive(session, message.getId(), WORKSPACE_SUBSCRIBE_ERROR, 400, ex.getMessage());
                });
    }
}
