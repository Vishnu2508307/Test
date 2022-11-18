package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.annotation.DeleteCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationdeleted.AnnotationDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteCoursewareAnnotationMessageHandler implements MessageHandler<DeleteCoursewareAnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_DELETE = "author.annotation.delete";
    public static final String AUTHOR_ANNOTATION_DELETE_OK = "author.annotation.delete.ok";
    public static final String AUTHOR_ANNOTATION_DELETE_ERROR = "author.annotation.delete.error";

    private final AnnotationService annotationService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AnnotationDeletedRTMProducer annotationDeletedRTMProducer;

    @Inject
    public DeleteCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                    final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                    final AuthenticationContextProvider authenticationContextProvider,
                                                    final Provider<RTMClientContext> rtmClientContextProvider,
                                                    final AnnotationDeletedRTMProducer annotationDeletedRTMProducer) {
        this.annotationService = annotationService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.annotationDeletedRTMProducer = annotationDeletedRTMProducer;
    }

    @Override
    public void validate(DeleteCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getAnnotationId() != null, "missing annotationId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_DELETE)
    @Override
    public void handle(Session session, DeleteCoursewareAnnotationMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        annotationService.findCoursewareAnnotation(message.getAnnotationId())
                .flatMap(coursewareAnnotation -> annotationService.deleteAnnotation(coursewareAnnotation)
                        .singleOrEmpty()
                        .thenReturn(coursewareAnnotation))
                .doOnEach(log.reactiveErrorThrowable("error deleting the annotation"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(coursewareAnnotation -> {
                               annotationDeletedRTMProducer.buildAnnotationDeletedRTMConsumable(rtmClientContext,
                                                                                                coursewareAnnotation.getRootElementId(),
                                                                                                message.getElementId(),
                                                                                                message.getElementType(),
                                                                                                message.getAnnotationId()).produce();
                           }, ex -> {
                               log.jsonDebug("Unable to delete annotation", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting the annotation");
                           },
                           () -> {
                               Responses.writeReactive(session,
                                                       new BasicResponseMessage(AUTHOR_ANNOTATION_DELETE_OK,
                                                                                message.getId()));
                               CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                       .setParentElement(null)
                                       .setElement(CoursewareElement.from(message.getElementId(),
                                                                          message.getElementType()))
                                       .setAccountId(account.getId())
                                       .setAnnotationId(message.getAnnotationId())
                                       .setAction(CoursewareAction.ANNOTATION_DELETED);
                               rtmEventBroker.broadcast(AUTHOR_ANNOTATION_DELETE, broadcastMessage);
                           });
    }
}
