package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class WorkspaceUnsubscribeMessageHandler implements MessageHandler<WorkspaceGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceUnsubscribeMessageHandler.class);

    public static final String WORKSPACE_UNSUBSCRIBE = "workspace.unsubscribe";
    private static final String WORKSPACE_UNSUBSCRIBE_OK = "workspace.unsubscribe.ok";
    private static final String WORKSPACE_UNSUBSCRIBE_ERROR = "workspace.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final WorkspaceRTMSubscriptionFactory workspaceRTMSubscriptionFactory;

    @Inject
    public WorkspaceUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
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

        try {

            rtmSubscriptionManagerProvider.get().unsubscribe(workspaceRTMSubscriptionFactory.create(message.getWorkspaceId()).getName());

            Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_UNSUBSCRIBE_OK, message.getId()));
        } catch (Throwable t) {
            log.jsonDebug( t.getMessage(), new HashMap<String, Object>(){
                {put("workspaceId", message.getWorkspaceId());}
            });
            ErrorMessage error = new ErrorMessage(WORKSPACE_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage("error unsubscribing from workspace subscription")
                    .setCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
            Responses.write(session, error);
        }
    }
}
