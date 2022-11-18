package com.smartsparrow.rtm.message.handler.courseware.scope;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.scope.StudentScopeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeRegisterFromStudentScopeMessageHandler implements MessageHandler<StudentScopeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeRegisterFromStudentScopeMessageHandler.class);

    public static final String AUTHOR_STUDENT_SCOPE_DEREGISTER = "author.student.scope.deregister";
    private static final String AUTHOR_STUDENT_SCOPE_DEREGISTER_OK = "author.student.scope.deregister.ok";
    private static final String AUTHOR_STUDENT_SCOPE_DEREGISTER_ERROR = "author.student.scope.deregister.error";

    private final CoursewareService coursewareService;

    @Inject
    public DeRegisterFromStudentScopeMessageHandler(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(StudentScopeMessage message) throws RTMValidationException {

        try {
            checkArgument(message.getStudentScopeURN() != null, "studentScopeURN is required");
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_STUDENT_SCOPE_DEREGISTER_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_STUDENT_SCOPE_DEREGISTER)
    public void handle(Session session, StudentScopeMessage message) throws WriteResponseException {

        coursewareService.deRegister(message.getStudentScopeURN(), message.getElementId())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while de-registering from student scope"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(aVoid -> {
                    // nothing happens here in this void
                }, ex -> {
                    log.error("Exception while de-registering", ex);
                    Responses.errorReactive(session, message.getId(), AUTHOR_STUDENT_SCOPE_DEREGISTER_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error de-registering");
                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_STUDENT_SCOPE_DEREGISTER_OK, message.getId()));
                });
    }
}
