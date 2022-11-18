package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.eventmessage.ProjectChangeLogEventMessage;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ProjectChangeLogUnsubscribeMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectChangeLogSubscribeMessageHandler.class);

    public static final String PROJECT_CHANGELOG_UNSUBSCRIBE = "project.changelog.unsubscribe";
    private static final String PROJECT_CHANGELOG_UNSUBSCRIBE_OK = "project.changelog.unsubscribe.ok";
    private static final String PROJECT_CHANGELOG_UNSUBSCRIBE_ERROR = "project.changelog.unsubscribe.error";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;

    @Inject
    public ProjectChangeLogUnsubscribeMessageHandler(final Provider<SubscriptionManager> subscriptionManagerProvider) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
    }

    @Override
    public void validate(ProjectGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {
        final SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        try {
            subscriptionManager.unsubscribe(new ProjectChangeLogEventMessage(message.getProjectId()).getName());
            Responses.write(session, new BasicResponseMessage(PROJECT_CHANGELOG_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(PROJECT_CHANGELOG_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Changelog Subscription for project %s not found", message.getProjectId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
