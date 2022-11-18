package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ResolveCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ResolveCoursewareAnnotationMessageHandler implements MessageHandler<ResolveCoursewareAnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ResolveCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_RESOLVE = "author.annotation.resolve";
    public static final String AUTHOR_ANNOTATION_RESOLVE_OK = "author.annotation.resolve.ok";
    public static final String AUTHOR_ANNOTATION_RESOLVE_ERROR = "author.annotation.resolve.error";

    private final AnnotationService annotationService;

    @Inject
    public ResolveCoursewareAnnotationMessageHandler(final AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    public void validate(ResolveCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "rootElementId is required");
        affirmArgument(message.getCoursewareAnnotationKeys() != null, "coursewareAnnotationKeys is required");
        affirmArgument(message.getResolved() != null, "resolved is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_RESOLVE)
    @Override
    public void handle(Session session, ResolveCoursewareAnnotationMessage message) throws WriteResponseException {
        annotationService.resolveComments(message.getCoursewareAnnotationKeys(), message.getResolved())
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error while resolving comment annotations"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(v -> {
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_RESOLVE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error resolving comment annotations");
                }, () -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_ANNOTATION_RESOLVE_OK, message.getId()));
                });
    }
}
