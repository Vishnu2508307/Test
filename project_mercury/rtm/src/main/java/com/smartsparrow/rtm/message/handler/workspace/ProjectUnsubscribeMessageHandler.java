package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ProjectUnsubscribeMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectUnsubscribeMessageHandler.class);

    public static final String WORKSPACE_PROJECT_UNSUBSCRIBE = "workspace.project.unsubscribe";
    private static final String WORKSPACE_PROJECT_UNSUBSCRIBE_OK = "workspace.project.unsubscribe.ok";
    private static final String WORKSPACE_PROJECT_UNSUBSCRIBE_ERROR = "workspace.project.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory;

    @Inject
    public ProjectUnsubscribeMessageHandler(final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                            final ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.projectEventRTMSubscriptionFactory = projectEventRTMSubscriptionFactory;
    }

    @Override
    public void validate(ProjectGenericMessage message) {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {
        try {
            ProjectEventRTMSubscription projectEventRTMSubscription = projectEventRTMSubscriptionFactory.create(message.getProjectId());
            rtmSubscriptionManagerProvider.get().unsubscribe(projectEventRTMSubscription.getName());
            Responses.write(session, new BasicResponseMessage(WORKSPACE_PROJECT_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found ", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(WORKSPACE_PROJECT_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for project %s not found", message.getProjectId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
