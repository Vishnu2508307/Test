package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.courseware.ActivityChangeLogSubscription;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Mono;

public class ActivityChangeLogSubscribeMessageHandler implements MessageHandler<ActivityGenericMessage> {

    public static final String ACTIVITY_CHANGELOG_SUBSCRIBE = "project.activity.changelog.subscribe";
    private static final String ACTIVITY_CHANGELOG_SUBSCRIBE_OK = "project.activity.changelog.subscribe.ok";
    private static final String ACTIVITY_CHANGELOG_SUBSCRIBE_ERROR = "project.activity.changelog.subscribe.error";
    public static final String ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST = "project.activity.changelog.broadcast";
    public static final String ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR = "project.activity.changelog.broadcast.error";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;
    private final ActivityChangeLogSubscription activityChangeLogSubscription;

    @Inject
    public ActivityChangeLogSubscribeMessageHandler(final Provider<SubscriptionManager> subscriptionManagerProvider,
                                                    final ActivityChangeLogSubscription activityChangeLogSubscription) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
        this.activityChangeLogSubscription = activityChangeLogSubscription;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activityId is required");
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {
        final SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        activityChangeLogSubscription.setActivityId(message.getActivityId());

        Mono<Integer> add = subscriptionManager.add(activityChangeLogSubscription);

        add.subscribe(listenerId -> {
            // nothing to do here
        }, this::subscriptionOnErrorHandler, () -> {
            BasicResponseMessage response = new BasicResponseMessage(ACTIVITY_CHANGELOG_SUBSCRIBE_OK, message.getId());
            response.addField("rtmSubscriptionId", activityChangeLogSubscription.getId());
            Responses.writeReactive(session, response);
        });
    }
}
