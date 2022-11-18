package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

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
import com.smartsparrow.rtm.message.recv.courseware.annotation.GetCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetCoursewareAnnotationMessageHandler implements MessageHandler<GetCoursewareAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_GET = "author.annotation.get";
    public static final String AUTHOR_ANNOTATION_GET_OK = "author.annotation.get.ok";
    public static final String AUTHOR_ANNOTATION_GET_ERROR = "author.annotation.get.error";

    private final AnnotationService annotationService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public GetCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                 final Provider<AuthenticationContext> authenticationContextProvider) {
        this.annotationService = annotationService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(GetCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getAnnotationId() != null, "missing annotationId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, GetCoursewareAnnotationMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        annotationService.findCoursewareAnnotation(message.getAnnotationId(), account.getId())
                .doOnEach(log.reactiveErrorThrowable("error fetching courseware annotation", throwable -> new HashMap<String, Object>() {
                    {
                        put("annotationId", message.getAnnotationId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(coursewareAnnotation -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    AUTHOR_ANNOTATION_GET_OK,
                                    message.getId());
                            basicResponseMessage.addField("coursewareAnnotation", coursewareAnnotation);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> {
                            log.jsonDebug("Unable to fetch the courseware annotation", new HashMap<String, Object>() {
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_GET_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to fetch courseware annotation");
                        }
                );
    }
}
