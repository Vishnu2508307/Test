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
import com.smartsparrow.rtm.message.recv.courseware.annotation.ListCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class AggregateCoursewareAnnotationMessageHandler implements MessageHandler<ListCoursewareAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AggregateCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_AGGREGATE = "author.annotation.aggregate";
    public static final String AUTHOR_ANNOTATION_AGGREGATE_OK = "author.annotation.aggregate.ok";
    public static final String AUTHOR_ANNOTATION_AGGREGATE_ERROR = "author.annotation.aggregate.error";

    private final AnnotationService annotationService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public AggregateCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                       final Provider<AuthenticationContext> authenticationContextProvider) {
        this.annotationService = annotationService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(ListCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "missing rootElementId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_AGGREGATE)
    @Override
    public void handle(Session session, ListCoursewareAnnotationMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        annotationService.aggregateCoursewareAnnotation(message.getRootElementId(),
                                                        message.getElementId(),
                                                        account.getId())
                .doOnEach(log.reactiveErrorThrowable("error aggregating courseware annotation",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("rootElementId", message.getRootElementId());
                                                             put("motivation", message.getMotivation());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(coursewareAnnotationAggregate -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ANNOTATION_AGGREGATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("coursewareAnnotationAggregate", coursewareAnnotationAggregate);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to aggregate the courseware annotation",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ANNOTATION_AGGREGATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to aggregate courseware annotation");
                           }
                );
    }
}
