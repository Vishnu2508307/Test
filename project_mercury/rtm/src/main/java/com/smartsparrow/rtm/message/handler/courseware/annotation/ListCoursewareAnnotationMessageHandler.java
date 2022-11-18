package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotationPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ListCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

public class ListCoursewareAnnotationMessageHandler implements MessageHandler<ListCoursewareAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_LIST = "author.annotation.list";
    public static final String AUTHOR_ANNOTATION_LIST_OK = "author.annotation.list.ok";
    public static final String AUTHOR_ANNOTATION_LIST_ERROR = "author.annotation.list.error";

    private final AnnotationService annotationService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public ListCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                  final Provider<AuthenticationContext> authenticationContextProvider) {
        this.annotationService = annotationService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(ListCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "missing rootElementId");
        affirmArgument(message.getMotivation() != null, "missing motivation");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_LIST)
    @Override
    public void handle(Session session, ListCoursewareAnnotationMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        Flux<CoursewareAnnotationPayload> coursewareAnnotationFlux;
        if (message.getElementId() == null) {
            coursewareAnnotationFlux = annotationService.fetchCoursewareAnnotation(message.getRootElementId(),
                                                                                   message.getMotivation(),
                                                                                   account.getId());
        } else {
            coursewareAnnotationFlux = annotationService.fetchCoursewareAnnotation(message.getRootElementId(),
                                                                                   message.getElementId(),
                                                                                   message.getMotivation(),
                                                                                   account.getId());
        }
        coursewareAnnotationFlux
                .doOnEach(log.reactiveErrorThrowable("error fetching courseware annotation", throwable -> new HashMap<String, Object>() {
                    {
                        put("elementId", message.getElementId());
                        put("motivation", message.getMotivation());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(coursewareAnnotations -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    AUTHOR_ANNOTATION_LIST_OK,
                                    message.getId());
                            basicResponseMessage.addField("coursewareAnnotation", coursewareAnnotations);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> {
                            log.jsonDebug("Unable to fetch the courseware annotation", new HashMap<String, Object>() {
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_LIST_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to fetch courseware annotation");
                        }
                );
    }
}
