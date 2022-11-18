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
import com.smartsparrow.rtm.message.recv.courseware.annotation.UpdateCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UpdateCoursewareAnnotationMessageHandler implements MessageHandler<UpdateCoursewareAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_UPDATE = "author.annotation.update";
    public static final String AUTHOR_ANNOTATION_UPDATE_OK = "author.annotation.update.ok";
    public static final String AUTHOR_ANNOTATION_UPDATE_ERROR = "author.annotation.update.error";

    private final AnnotationService annotationService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AnnotationUpdatedRTMProducer annotationUpdatedRTMProducer;

    @Inject
    public UpdateCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                    final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                    final AuthenticationContextProvider authenticationContextProvider,
                                                    final Provider<RTMClientContext> rtmClientContextProvider,
                                                    final AnnotationUpdatedRTMProducer annotationUpdatedRTMProducer) {
        this.annotationService = annotationService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.annotationUpdatedRTMProducer = annotationUpdatedRTMProducer;
    }

    @Override
    public void validate(UpdateCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getAnnotationId() != null, "missing annotation id");
        affirmArgument(message.getBody() != null, "missing annotation body");
        affirmArgument(message.getTarget() != null, "missing annotation target");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, UpdateCoursewareAnnotationMessage message) throws WriteResponseException {

        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        annotationService.updateCoursewareAnnotation(message.getAnnotationId(), message.getBody(), message.getTarget())
                .doOnEach(log.reactiveErrorThrowable("error updating courseware annotation", throwable -> new HashMap<String, Object>() {
                    {
                        put("annotationId", message.getAnnotationId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(coursewareAnnotation -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    AUTHOR_ANNOTATION_UPDATE_OK,
                                    message.getId());
                            basicResponseMessage.addField("coursewareAnnotation", coursewareAnnotation);
                            Responses.writeReactive(session, basicResponseMessage);

                            CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                   .setParentElement(null)
                                   .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                                   .setAccountId(account.getId())
                                   .setAnnotationId(message.getAnnotationId())
                                   .setAction(CoursewareAction.ANNOTATION_UPDATED);
                            rtmEventBroker.broadcast(AUTHOR_ANNOTATION_UPDATE, broadcastMessage);
                            annotationUpdatedRTMProducer.buildAnnotationUpdatedRTMConsumable(rtmClientContext,
                                                                                             coursewareAnnotation.getRootElementId(),
                                                                                             message.getElementId(),
                                                                                             message.getElementType(),
                                                                                             message.getAnnotationId()).produce();
                        },
                        ex -> {
                            log.jsonDebug("Unable to update the courseware annotation", new HashMap<String, Object>() {
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_UPDATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "Unable to update courseware annotation");
                        }
                );
    }
}
