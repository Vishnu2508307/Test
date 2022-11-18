package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ReadCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ReadCoursewareAnnotationMessageHandler implements MessageHandler<ReadCoursewareAnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReadCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_READ = "author.annotation.read";
    public static final String AUTHOR_ANNOTATION_READ_OK = "author.annotation.read.ok";
    public static final String AUTHOR_ANNOTATION_READ_ERROR = "author.annotation.read.error";

    private final AnnotationService annotationService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ReadCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                  final Provider<AuthenticationContext> authenticationContextProvider) {
        this.annotationService = annotationService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(ReadCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "rootElementId is required");
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
        affirmArgument(message.getAnnotationIds() != null, "annotationIds is required");
        affirmArgument(message.getRead() != null, "read is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_READ)
    @Override
    public void handle(Session session, ReadCoursewareAnnotationMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        annotationService.readComments(message.getRootElementId(), message.getElementId(), message.getAnnotationIds(),
                                       message.getRead(), account.getId())
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error while marking comment annotations as " +
                                                             (message.getRead() ? "read" : "unread")))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(v -> {
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_READ_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error marking comment annotations as " +
                                                    (message.getRead() ? "read" : "unread"));
                }, () -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_ANNOTATION_READ_OK, message.getId()));
                });
    }
}
