package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.StudentGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.StudentWalkablePrefetchRTMSubscription;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class StudentPrefetchSubscribeMessageHandler implements MessageHandler<StudentGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StudentPrefetchSubscribeMessageHandler.class);

    public static final String LEARNER_STUDENT_PREFETCH_SUBSCRIBE = "learner.student.walkable.prefetch.subscribe";
    public static final String LEARNER_STUDENT_PREFETCH_SUBSCRIBE_OK = "learner.student.walkable.prefetch.subscribe.ok";
    public static final String LEARNER_STUDENT_PREFETCH_SUBSCRIBE_ERROR = "learner.student.walkable.prefetch.subscribe.error";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory studentWalkablePrefetchRTMSubscriptionFactory;

    @Inject
    public StudentPrefetchSubscribeMessageHandler(Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                                  final StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory studentWalkablePrefetchRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.studentWalkablePrefetchRTMSubscriptionFactory = studentWalkablePrefetchRTMSubscriptionFactory;
    }

    @Override
    public void validate(StudentGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getStudentId() != null, "studentId is required");
    }

    @Override
    public void handle(Session session, StudentGenericMessage message) throws WriteResponseException {
        StudentWalkablePrefetchRTMSubscription studentWalkablePrefetchRTMSubscription =
                studentWalkablePrefetchRTMSubscriptionFactory.create(message.getStudentId());

        rtmSubscriptionManagerProvider.get().add(studentWalkablePrefetchRTMSubscription)
                .subscribe(listenerId -> {
                    BasicResponseMessage response = new BasicResponseMessage(LEARNER_STUDENT_PREFETCH_SUBSCRIBE_OK, message.getId());
                    response.addField("rtmSubscriptionId", studentWalkablePrefetchRTMSubscription.getId());
                    Responses.writeReactive(session, response);
                }, ex -> {
                    log.jsonError(ex.getMessage(), new HashMap<>(), ex);
                    Responses.errorReactive(session, message.getId(), LEARNER_STUDENT_PREFETCH_SUBSCRIBE_ERROR, 400, ex.getMessage());
                });
    }
}
