package com.smartsparrow.rtm.message.handler.courseware.scope;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.CoursewareElementNotFoundFault;
import com.smartsparrow.courseware.lang.CoursewareException;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class RegisterToStudentScopeMessageHandler implements MessageHandler<StudentScopeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RegisterToStudentScopeMessageHandler.class);

    public static final String AUTHOR_STUDENT_SCOPE_REGISTER = "author.student.scope.register";
    private static final String AUTHOR_STUDENT_SCOPE_REGISTER_OK = "author.student.scope.register.ok";
    private static final String AUTHOR_STUDENT_SCOPE_REGISTER_ERROR = "author.student.scope.register.error";

    private final CoursewareService coursewareService;

    @Inject
    public RegisterToStudentScopeMessageHandler(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    /**
     * Validate that the incoming message is valid for registering an element to a student scope.
     * <ul>
     *     <li>Checks the validaity of the supplied message parameters</li>
     *     <li>Check that an element can be found for the supplied student scope urn</li>
     *     <li>Check that a parent path can be found for the supplied courseware element details</li>
     *     <li>Check that the student scope belongs to an element in the parent path</li>
     *     <li>Check that the element type has a plugin reference</li>
     * </ul>
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the validation fails
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "method findElementByScope never returns an empty element and element is checked for null")
    @Override
    public void validate(StudentScopeMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
            checkArgument(message.getStudentScopeURN() != null, "studentScopeURN is required");

            CoursewareElement element = coursewareService.findElementByStudentScope(message.getStudentScopeURN()).block();
            checkArgument(element != null, "element not found for supplied student scope");
            checkArgument(CoursewareElementType.isAWalkable(element.getElementType()),
                    String.format("element associated with a student scope urn must be a walkable, found %s instead",
                            element.getElementType()));
            List<CoursewareElement> path = coursewareService.getPath(message.getElementId(), message.getElementType()).block();

            checkArgument(path != null, "could not find parent path for supplied element");
            checkArgument(path.contains(element),
                    String.format("cannot register element with student scope `%s` that is not in the parent path",
                            message.getStudentScopeURN()));
            checkArgument(CoursewareElementType.isAPluginReferenceType(message.getElementType()),
                    "only elementType with a plugin reference can be registered");
        } catch (IllegalArgumentException | UnsupportedOperationException | CoursewareException | CoursewareElementNotFoundFault e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_STUDENT_SCOPE_REGISTER_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_STUDENT_SCOPE_REGISTER)
    public void handle(Session session, StudentScopeMessage message) throws WriteResponseException {
        coursewareService.findPluginReference(message.getElementId(), message.getElementType())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while registering student scope"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .flatMap(pluginReference ->
                        coursewareService.register(message.getStudentScopeURN(), pluginReference, message.getElementId(),
                                message.getElementType()))
                .subscribe(scopeReference-> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_STUDENT_SCOPE_REGISTER_OK, message.getId()));
                }, ex -> {
                    log.debug("Unexpected error", ex);
                    Responses.errorReactive(session, message.getId(), AUTHOR_STUDENT_SCOPE_REGISTER_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error registering");
                });

    }
}
