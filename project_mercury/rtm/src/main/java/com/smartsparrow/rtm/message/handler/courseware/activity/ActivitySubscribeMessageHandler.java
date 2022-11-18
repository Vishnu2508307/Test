package com.smartsparrow.rtm.message.handler.courseware.activity;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription.ActivityRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ActivitySubscribeMessageHandler implements MessageHandler<ActivityGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivitySubscribeMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_SUBSCRIBE = "author.activity.subscribe";
    private static final String AUTHOR_ACTIVITY_SUBSCRIBE_OK = "author.activity.subscribe.ok";
    private static final String AUTHOR_ACTIVITY_SUBSCRIBE_ERROR = "author.activity.subscribe.error";
    public static final String AUTHOR_ACTIVITY_BROADCAST = "author.activity.broadcast";
    public static final String AUTHOR_ACTIVITY_BROADCAST_ERROR = "author.activity.broadcast.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory;

    @Inject
    public ActivitySubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                           ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.activityRTMSubscriptionFactory = activityRTMSubscriptionFactory;
    }

    @Override
    public void validate(ActivityGenericMessage message) throws RTMValidationException {
        if (message.getActivityId() == null) {
            throw new RTMValidationException("activityId is required", message.getId(), AUTHOR_ACTIVITY_SUBSCRIBE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ACTIVITY_SUBSCRIBE)
    @Override
    public void handle(Session session, ActivityGenericMessage message) throws WriteResponseException {
        ActivityRTMSubscription activityRTMSubscription = activityRTMSubscriptionFactory.create(message.getActivityId());
        rtmSubscriptionManagerProvider.get().add(activityRTMSubscription)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("exception occurred while adding the subscription"))
                .subscribe(listenerId -> {},
                           this::subscriptionOnErrorHandler,
                           () -> {
                               log.jsonDebug("client subscribing to events ", new HashMap<String, Object>() {
                                   {
                                       put("activityId", message.getActivityId());
                                   }
                               });
                               Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ACTIVITY_SUBSCRIBE_OK, message.getId())
                                                               .addField("rtmSubscriptionId", activityRTMSubscription.getId()));
                           });

    }

}
