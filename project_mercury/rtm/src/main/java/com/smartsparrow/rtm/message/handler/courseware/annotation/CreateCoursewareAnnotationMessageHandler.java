package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.lang.AnnotationAlreadyExistsFault;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.annotation.CreateCoursewareAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationcreated.AnnotationCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

public class CreateCoursewareAnnotationMessageHandler implements MessageHandler<CreateCoursewareAnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateCoursewareAnnotationMessageHandler.class);

    public static final String AUTHOR_ANNOTATION_CREATE = "author.annotation.create";
    public static final String AUTHOR_ANNOTATION_CREATE_OK = "author.annotation.create.ok";
    public static final String AUTHOR_ANNOTATION_CREATE_ERROR = "author.annotation.create.error";

    private final AnnotationService annotationService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AnnotationCreatedRTMProducer annotationCreatedRTMProducer;

    @Inject
    public CreateCoursewareAnnotationMessageHandler(final AnnotationService annotationService,
                                                    final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                    final Provider<AuthenticationContext> authenticationContextProvider,
                                                    final Provider<RTMClientContext> rtmClientContextProvider,
                                                    final AnnotationCreatedRTMProducer annotationCreatedRTMProducer) {
        this.annotationService = annotationService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.annotationCreatedRTMProducer = annotationCreatedRTMProducer;
    }

    @Override
    public void validate(CreateCoursewareAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "missing rootElementId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getMotivation() != null, "missing motivation");
        affirmArgument(message.getBody() != null, "missing body");
        affirmArgument(message.getTarget() != null, "missing target");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ANNOTATION_CREATE)
    @Override
    public void handle(Session session, CreateCoursewareAnnotationMessage message) throws WriteResponseException {

        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();

        UUID id = UUIDs.timeBased();
        JsonNode bodyNode = Json.toJsonNode(message.getBody(), "invalid body json");
        JsonNode targetNode = Json.toJsonNode(message.getTarget(), "invalid target json");

        CoursewareAnnotation coursewareAnnotation = new CoursewareAnnotation() //
                .setId(id) //
                .setVersion(id) //
                .setMotivation(message.getMotivation()) //
                .setRootElementId(message.getRootElementId()) //
                .setElementId(message.getElementId()) //
                .setBodyJson(bodyNode) //
                .setTargetJson(targetNode) //
                .setCreatorAccountId(account.getId())
                .setResolved(false);

        Flux<Void> annotationFlux;
        if (message.getAnnotationId() == null) {
            annotationFlux = annotationService.create(coursewareAnnotation);
        } else {
            annotationFlux = annotationService.create(coursewareAnnotation, message.getAnnotationId());
        }
        annotationFlux
                .doOnEach(log.reactiveErrorThrowable("error creating the annotation"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                    // nothing here, never executed
                }, ex -> {
                    log.jsonDebug("Unable to create annotation", new HashMap<String, Object>(){
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });

                    String errorMessage = "error creating the annotation";
                    int code = HttpStatus.SC_UNPROCESSABLE_ENTITY;

                    if (ex instanceof AnnotationAlreadyExistsFault) {
                        code = HttpStatus.SC_CONFLICT;
                        errorMessage = String.format("Annotation id %s already exists", message.getAnnotationId());
                    }

                    Responses.errorReactive(session, message.getId(), AUTHOR_ANNOTATION_CREATE_ERROR, code, errorMessage);
                }, ()->  {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ANNOTATION_CREATE_OK, message.getId())
                        .addField("coursewareAnnotation", coursewareAnnotation));
                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setParentElement(null)
                            .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                            .setAccountId(account.getId())
                            .setAnnotationId(message.getAnnotationId() == null ? coursewareAnnotation.getId() : message.getAnnotationId())
                            .setAction(CoursewareAction.ANNOTATION_CREATED);
                    rtmEventBroker.broadcast(AUTHOR_ANNOTATION_CREATE, broadcastMessage);
                    annotationCreatedRTMProducer.buildAnnotationCreatedRTMConsumable(rtmClientContext,
                                                                                     message.getRootElementId(),
                                                                                     message.getElementId(),
                                                                                     message.getElementType(),
                                                                                     message.getAnnotationId() == null ? coursewareAnnotation.getId() : message.getAnnotationId()).produce();
                });

    }
}
