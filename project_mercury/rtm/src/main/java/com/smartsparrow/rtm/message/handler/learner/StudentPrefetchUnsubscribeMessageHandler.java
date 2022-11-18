package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.StudentGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.StudentWalkablePrefetchRTMSubscription;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class StudentPrefetchUnsubscribeMessageHandler implements MessageHandler<StudentGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StudentPrefetchUnsubscribeMessageHandler.class);

    public static final String LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE = "learner.student.walkable.prefetch.unsubscribe";
    private static final String LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE_OK = "learner.student.walkable.prefetch.unsubscribe.ok";
    private static final String LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE_ERROR = "learner.student.walkable.prefetch.unsubscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory studentWalkablePrefetchRTMSubscriptionFactory;

    @Inject
    public StudentPrefetchUnsubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                    final StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory studentWalkablePrefetchRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.studentWalkablePrefetchRTMSubscriptionFactory = studentWalkablePrefetchRTMSubscriptionFactory;
    }

    @Override
    public void validate(StudentGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getStudentId() != null, "workspaceId is required");
    }

    @Override
    public void handle(Session session, StudentGenericMessage message) throws WriteResponseException {

        try {
            StudentWalkablePrefetchRTMSubscription studentWalkablePrefetchRTMSubscription =
                    studentWalkablePrefetchRTMSubscriptionFactory.create(message.getStudentId());

            rtmSubscriptionManagerProvider.get().unsubscribe(studentWalkablePrefetchRTMSubscription.getName());

            Responses.writeReactive(session, new BasicResponseMessage(LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE_OK, message.getId()));
        } catch (Throwable t) {
            log.jsonDebug( t.getMessage(), new HashMap<String, Object>(){
                {put("studentId", message.getStudentId());}
            });
            ErrorMessage error = new ErrorMessage(LEARNER_STUDENT_PREFETCH_UNSUBSCRIBE_ERROR)
                    .setReplyTo(message.getId())
                    .setMessage("error unsubscribing from student prefetch subscription")
                    .setCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
            Responses.write(session, error);
        }
    }
}
