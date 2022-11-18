package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.DuplicateInteractiveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.duplicated.InteractiveDuplicatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class DuplicateInteractiveMessageHandler implements MessageHandler<DuplicateInteractiveMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(DuplicateInteractiveMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_DUPLICATE = "author.interactive.duplicate";
    public static final String AUTHOR_INTERACTIVE_DUPLICATE_OK = "author.interactive.duplicate.ok";
    public static final String AUTHOR_INTERACTIVE_DUPLICATE_ERROR = "author.interactive.duplicate.error";

    private final InteractiveService interactiveService;
    private final PathwayService pathwayService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final InteractiveDuplicatedRTMProducer interactiveDuplicatedRTMProducer;

    @Inject
    public DuplicateInteractiveMessageHandler(final InteractiveService interactiveService,
                                              final PathwayService pathwayService,
                                              final CoursewareService coursewareService,
                                              final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                              final AuthenticationContextProvider authenticationContextProvider,
                                              final com.google.inject.Provider<RTMClientContext> rtmClientContextProvider,
                                              final InteractiveDuplicatedRTMProducer interactiveDuplicatedRTMProducer) {
        this.interactiveService = interactiveService;
        this.pathwayService = pathwayService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.interactiveDuplicatedRTMProducer = interactiveDuplicatedRTMProducer;
    }

    @Override
    public void validate(DuplicateInteractiveMessage message) throws RTMValidationException {
        if (message.getInteractiveId() == null) {
            throwError("missing interactiveId", message.getId());
        }

        if (message.getPathwayId() == null) {
            throwError("missing pathwayId", message.getId());
        }

        try {
            interactiveService.findById(message.getInteractiveId()).block();
        } catch (InteractiveNotFoundException e) {
            throwError("interactive not found", message.getId());
        }

        try {
            pathwayService.findById(message.getPathwayId()).block();
        } catch (PathwayNotFoundException e) {
            throwError("pathway not found", message.getId());
        }

        if (message.getIndex() != null && message.getIndex() < 0) {
            throwError("index should be >= 0", message.getId());
        }
    }

    private static void throwError(String errorMessage, String replyTo) throws RTMValidationException {
        logger.jsonDebug("Missing interactiveId",   new HashMap<String,Object>() {
            {
                put("errorMessage", errorMessage);
                put("messageId",replyTo);
            }
        });
        throw new RTMValidationException(errorMessage, replyTo, AUTHOR_INTERACTIVE_DUPLICATE_ERROR);
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_DUPLICATE)
    public void handle(Session session, DuplicateInteractiveMessage message) {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        Mono<Interactive> duplicateInteractiveMono;
        if (message.getIndex() == null) {
            duplicateInteractiveMono = coursewareService.duplicateInteractive(message.getInteractiveId(),
                    message.getPathwayId(), account.getId())
                            .doOnEach(logger.reactiveErrorThrowable("Error while pulling the DuplicateInteractive", throwable -> new HashMap<String, Object>() {
                                {
                                    put("message",message.toString());
                                }
                            })
            )
            // link each signal to the current transaction token
            .doOnEach(ReactiveTransaction.linkOnNext())
            // expire the transaction token on completion
            .doOnEach(ReactiveTransaction.expireOnComplete())
            // create a reactive context that enables all supported reactive monitoring
            .subscriberContext(ReactiveMonitoring.createContext());
        } else {
            duplicateInteractiveMono = coursewareService.duplicateInteractive(message.getInteractiveId(),
                    message.getPathwayId(), message.getIndex())
                    .doOnEach(logger.reactiveErrorThrowable("Error while pulling the DuplicateInteractive with Message ", throwable -> new HashMap<String, Object>() {
                            {
                                put("message",message.toString());
                            }
                        }))
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext());
        }

        Mono<InteractivePayload> interactivePayloadMono = duplicateInteractiveMono
                .flatMap(interactiveService::getInteractivePayload)
                .single()
                .doOnEach(logger.reactiveErrorThrowable("Traversing duplicateInteractive ",
                                                        duplicateinteractiveMessage -> new HashMap<String, Object>() {
                                                            {
                                                                put("interactiveID", message.getInteractiveId());
                                                            }
                                                        })
                )
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getPathwayId(), PATHWAY);
        Mono.zip(interactivePayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_DUPLICATE_OK,
                                                                                         message.getId());
                    basicResponseMessage.addField("interactive", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);
                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setAccountId(account.getId())
                            .setParentElement(CoursewareElement.from(message.getPathwayId(), PATHWAY))
                            .setElement(CoursewareElement.from(tuple2.getT1().getInteractiveId(),
                                                               CoursewareElementType.INTERACTIVE))
                            .setAction(CoursewareAction.DUPLICATED);

                    rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_DUPLICATE, broadcastMessage);
                    interactiveDuplicatedRTMProducer.buildInteractiveDuplicatedRTMConsumable(rtmClientContext,
                                                                                             tuple2.getT2(),
                                                                                             tuple2.getT1().getInteractiveId(),
                                                                                             message.getPathwayId()).produce();
                }, ex -> {
                    ex = Exceptions.unwrap(ex);
                    if (ex instanceof IndexOutOfBoundsException) {
                        Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_DUPLICATE_ERROR,
                                                HttpStatus.SC_BAD_REQUEST, "Index is out of range");
                    } else {
                        Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_DUPLICATE_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to duplicate interactive");
                    }
                    logger.jsonDebug("Interactive with id can't be duplicated", new HashMap<String, Object>() {
                        {
                            put("interactiveId", message.getInteractiveId());
                        }
                    });
                });
    }
}
