package com.smartsparrow.rtm.message.handler.courseware.activity;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription.ActivityRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ActivityUnsubscribeMessageHandler implements MessageHandler<ActivityGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityUnsubscribeMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_UNSUBSCRIBE = "author.activity.unsubscribe";
    private static final String AUTHOR_ACTIVITY_UNSUBSCRIBE_ERROR = "author.activity.unsubscribe.error";
    private static final String AUTHOR_ACTIVITY_UNSUBSCRIBE_OK = "author.activity.unsubscribe.ok";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory;

    @Inject
    public ActivityUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                             final ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.activityRTMSubscriptionFactory = activityRTMSubscriptionFactory;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {
        if (message.getActivityId() == null) {
            throw new RTMValidationException("activityId is required", message.getId(), AUTHOR_ACTIVITY_UNSUBSCRIBE_ERROR);
        }
    }

    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {
        try {
            rtmSubscriptionManagerProvider.get().unsubscribe(activityRTMSubscriptionFactory.create(message.getActivityId()).getName());
            Responses.write(session, new BasicResponseMessage(AUTHOR_ACTIVITY_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.debug("subscription not found ", subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(AUTHOR_ACTIVITY_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for activity %s not found", message.getActivityId()))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
        }
    }
}
