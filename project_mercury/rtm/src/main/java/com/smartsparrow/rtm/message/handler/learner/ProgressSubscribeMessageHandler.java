package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.ProgressSubscribeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription.StudentProgressRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ProgressSubscribeMessageHandler implements MessageHandler<ProgressSubscribeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProgressSubscribeMessageHandler.class);

    public static final String LEARNER_PROGRESS_SUBSCRIBE = "learner.progress.subscribe";
    public static final String LEARNER_PROGRESS_SUBSCRIBE_OK = "learner.progress.subscribe.ok";
    public static final String LEARNER_PROGRESS_SUBSCRIBE_ERROR = "learner.progress.subscribe.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory;

    @Inject
    public ProgressSubscribeMessageHandler(AuthenticationContextProvider authenticationContextProvider,
                                           Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                           StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.studentProgressRTMSubscriptionFactory = studentProgressRTMSubscriptionFactory;
    }

    @Override
    public void validate(ProgressSubscribeMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
        affirmArgument(message.getCoursewareElementId() != null, "coursewareElementId is required");
    }

    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_PROGRESS_SUBSCRIBE)
    @Override
    public void handle(Session session, ProgressSubscribeMessage message) throws WriteResponseException {

        StudentProgressRTMSubscription studentProgressRTMSubscription = studentProgressRTMSubscriptionFactory.create(
                message.getDeploymentId(),
                message.getCoursewareElementId(),
                authenticationContextProvider.get().getAccount().getId());
        rtmSubscriptionManagerProvider.get().add(studentProgressRTMSubscription)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("exception occurred while adding the subscription"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(
                        listenerId -> {
                        },
                        this::subscriptionOnErrorHandler,
                        () -> {
                            BasicResponseMessage response = new BasicResponseMessage(LEARNER_PROGRESS_SUBSCRIBE_OK,
                                                                                     message.getId());
                            response.addField("rtmSubscriptionId", studentProgressRTMSubscription.getId());
                            Responses.writeReactive(session, response);
                        }
                );
    }
}
