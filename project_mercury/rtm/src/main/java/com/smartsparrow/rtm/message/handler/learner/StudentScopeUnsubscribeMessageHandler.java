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
import com.smartsparrow.rtm.message.recv.learner.StudentScopeSubscribeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMSubscription;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class StudentScopeUnsubscribeMessageHandler implements MessageHandler<StudentScopeSubscribeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StudentScopeUnsubscribeMessageHandler.class);

    public static final String LEARNER_STUDENT_SCOPE_UNSUBSCRIBE = "learner.student.scope.unsubscribe";
    public static final String LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_OK = "learner.student.scope.unsubscribe.ok";
    public static final String LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_ERROR = "learner.student.scope.unsubscribe.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory;

    @Inject
    public StudentScopeUnsubscribeMessageHandler(AuthenticationContextProvider authenticationContextProvider,
                                                 Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                 final StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.studentScopeRTMSubscriptionFactory = studentScopeRTMSubscriptionFactory;
    }

    @Override
    public void validate(StudentScopeSubscribeMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
    }

    @Override
    public void handle(Session session, StudentScopeSubscribeMessage message) throws WriteResponseException {
        UUID studentId = authenticationContextProvider.get().getAccount().getId();
        try {
            StudentScopeRTMSubscription studentScopeRTMSubscription = studentScopeRTMSubscriptionFactory.create(studentId, message.getDeploymentId());
            rtmSubscriptionManagerProvider.get().unsubscribe(studentScopeRTMSubscription.getName());
            Responses.write(session, new BasicResponseMessage(LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_OK, message.getId()));
        } catch (SubscriptionNotFound subscriptionNotFound) {
            log.jsonError("Subscription not found", new HashMap<String, Object>() {
                {
                    put("id", message.getId());
                    put("deploymentId", message.getDeploymentId());
                    put("studentId", studentId);
                }
            }, subscriptionNotFound);
            ErrorMessage error = new ErrorMessage(LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage(String.format("Subscription for student %s scope not found", studentId))
                    .setCode(HttpStatus.SC_NOT_FOUND);
            Responses.write(session, error);
            throw new NotFoundFault("Subscription not found");
        }
    }
}
