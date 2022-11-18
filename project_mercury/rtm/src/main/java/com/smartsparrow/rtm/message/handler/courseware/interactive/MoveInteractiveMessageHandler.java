package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.MoveInteractiveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.InteractiveMovedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class MoveInteractiveMessageHandler implements MessageHandler<MoveInteractiveMessage> {

    public static final String AUTHOR_INTERACTIVE_MOVE = "author.interactive.move";
    public static final String AUTHOR_INTERACTIVE_MOVE_OK = "author.interactive.move.ok";
    public static final String AUTHOR_INTERACTIVE_MOVE_ERROR = "author.interactive.move.error";
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MoveInteractiveMessageHandler.class);
    private final InteractiveService interactiveService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final InteractiveMovedRTMProducer interactiveMovedRTMProducer;

    @Inject
    public MoveInteractiveMessageHandler(final InteractiveService interactiveService,
                                         final CoursewareService coursewareService,
                                         final Provider<AuthenticationContext> authenticationContextProvider,
                                         final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                         final Provider<RTMClientContext> rtmClientContextProvider,
                                         final InteractiveMovedRTMProducer interactiveMovedRTMProducer) {
        this.interactiveService = interactiveService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.interactiveMovedRTMProducer = interactiveMovedRTMProducer;

    }

    private static void throwError(String errorMessage, String replyTo) throws RTMValidationException {

        log.jsonDebug("Error validating the message", new HashMap<String, Object>() {
            {
                put("errorMessage", errorMessage);
                put("messageId", replyTo);
            }
        });
        throw new RTMValidationException(errorMessage, replyTo, AUTHOR_INTERACTIVE_MOVE_ERROR);
    }

    @Override
    public void validate(MoveInteractiveMessage message) throws RTMValidationException {

        affirmArgument(message.getInteractiveId() != null, "interactiveId is required");
        affirmArgument(message.getPathwayId() != null, "pathwayId is required");
        affirmArgument(!(message.getIndex() != null && message.getIndex() < 0), "index should be >= 0");
        affirmArgument(interactiveService.getInteractivePayload(message.getInteractiveId()).block() != null,
                "interactive payload not found");
        affirmArgument(interactiveService.findParentPathwayId(message.getInteractiveId()).block() != null,
                "parent pathway not found");

    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_MOVE)
    public void handle(final Session session, final MoveInteractiveMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        // do this call blocking cause the unblocking counterpart will make this class too hard to read
        final UUID oldParentPathwayId = interactiveService.findParentPathwayId(message.getInteractiveId())
                .block();

        affirmArgument(oldParentPathwayId != null, "parent pathway id was not found");

        Mono<InteractivePayload> interactivePayloadMono;

        if (message.getIndex() == null) {
            interactivePayloadMono = interactiveService.move(message.getInteractiveId(),
                    message.getPathwayId(),
                    oldParentPathwayId);
        } else {
            interactivePayloadMono = interactiveService.move(message.getInteractiveId(),
                    message.getPathwayId(),
                    message.getIndex(),
                    oldParentPathwayId);
        }

        interactivePayloadMono
                .doOnEach(log.reactiveErrorThrowable("Error while moving an interactive",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("message", message.toString());
                                                             put("error", throwable.getStackTrace());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getPathwayId(), PATHWAY);
        Mono.zip(interactivePayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_MOVE_OK,
                                                                                                    message.getId());
                               basicResponseMessage.addField("interactive", tuple2.getT1());
                               Responses.writeReactive(session, basicResponseMessage);
                               CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                       .setAccountId(account.getId())
                                       .setOldParentElement(CoursewareElement.from(oldParentPathwayId, PATHWAY))
                                       .setParentElement(CoursewareElement.from(message.getPathwayId(),
                                                                                PATHWAY))
                                       .setElement(CoursewareElement.from(tuple2.getT1().getInteractiveId(),
                                                                          CoursewareElementType.INTERACTIVE))
                                       .setAction(CoursewareAction.INTERACTIVE_MOVED);

                               rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_MOVE, broadcastMessage);
                               interactiveMovedRTMProducer.buildInteractiveMovedRTMConsumable(rtmClientContext,
                                                                                              tuple2.getT2(),
                                                                                              tuple2.getT1().getInteractiveId(),
                                                                                              oldParentPathwayId,
                                                                                              message.getPathwayId()).produce();
                           },
                           ex -> {
                               ex = Exceptions.unwrap(ex);
                               String errorMessage = "Unable to move interactive";
                               int statusCode = HttpStatus.SC_UNPROCESSABLE_ENTITY;
                               if (ex instanceof IndexOutOfBoundsException) {
                                   errorMessage = "Index is out of range";
                                   statusCode = HttpStatus.SC_BAD_REQUEST;
                               } else if (ex instanceof ParentPathwayNotFoundException) {
                                   errorMessage = "Parent pathway not found for interactive id";
                                   statusCode = HttpStatus.SC_NOT_FOUND;
                               }
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_INTERACTIVE_MOVE_ERROR,
                                                       statusCode,
                                                       errorMessage);
                           });
    }
}
