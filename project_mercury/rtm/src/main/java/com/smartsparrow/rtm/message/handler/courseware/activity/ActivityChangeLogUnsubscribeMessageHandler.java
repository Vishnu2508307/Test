package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.eventmessage.ActivityChangeLogEventMessage;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ActivityChangeLogUnsubscribeMessageHandler implements MessageHandler<ActivityGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityChangeLogUnsubscribeMessageHandler.class);

    public static final String ACTIVITY_CHANGELOG_UNSUBSCRIBE = "project.activity.changelog.unsubscribe";
    private static final String ACTIVITY_CHANGELOG_UNSUBSCRIBE_OK = "project.activity.changelog.unsubscribe.ok";
    private static final String ACTIVITY_CHANGELOG_UNSUBSCRIBE_ERROR = "project.activity.changelog.unsubscribe.error";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;

    @Inject
    public ActivityChangeLogUnsubscribeMessageHandler(final Provider<SubscriptionManager> subscriptionManagerProvider) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activityId is required");
    }

    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {
        final SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        try {
            subscriptionManager.unsubscribe(new ActivityChangeLogEventMessage(message.getActivityId()).getName());
            Responses.write(session, new BasicResponseMessage(ACTIVITY_CHANGELOG_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(ACTIVITY_CHANGELOG_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Changelog Subscription for activity %s not found", message.getActivityId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
