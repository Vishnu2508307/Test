package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.ProgressSubscribeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription.StudentProgressRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ProgressUnsubscribeMessageHandler implements MessageHandler<ProgressSubscribeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProgressUnsubscribeMessageHandler.class);

    public static final String LEARNER_PROGRESS_UNSUBSCRIBE = "learner.progress.unsubscribe";
    public static final String LEARNER_PROGRESS_UNSUBSCRIBE_OK = "learner.progress.unsubscribe.ok";
    public static final String LEARNER_PROGRESS_UNSUBSCRIBE_ERROR = "learner.progress.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public ProgressUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                             AuthenticationContextProvider authenticationContextProvider,
                                             StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.studentProgressRTMSubscriptionFactory = studentProgressRTMSubscriptionFactory;
    }

    @Override
    public void validate(ProgressSubscribeMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
        affirmArgument(message.getCoursewareElementId() != null, "coursewareElementId is required");
    }

    @Override
    public void handle(Session session, ProgressSubscribeMessage message) throws WriteResponseException {
        UUID studentId = authenticationContextProvider.get().getAccount().getId();
        try {
            StudentProgressRTMSubscription studentProgressRTMSubscription = studentProgressRTMSubscriptionFactory.create(message.getDeploymentId(), message.getCoursewareElementId(),studentId);
            rtmSubscriptionManagerProvider.get().unsubscribe(studentProgressRTMSubscription.getName());

            BasicResponseMessage responseMessage = new BasicResponseMessage(LEARNER_PROGRESS_UNSUBSCRIBE_OK,
                                                                            message.getId());
            Responses.write(session, responseMessage);
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.jsonError("Subscription not found", new HashMap<String, Object>() {
                {
                    put("id", message.getId());
                    put("deploymentId", message.getDeploymentId());
                    put("coursewareElementId", message.getCoursewareElementId());
                }
            }, subscriptionNotFound);

            ErrorMessage error = new ErrorMessage(LEARNER_PROGRESS_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for student %s progress not found", studentId))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
            throw new NotFoundFault("Subscription not found");
        }
    }
}
