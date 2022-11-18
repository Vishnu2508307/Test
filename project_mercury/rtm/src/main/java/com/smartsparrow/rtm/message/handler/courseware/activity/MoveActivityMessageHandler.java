package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
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
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.MoveActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.ActivityMovedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class MoveActivityMessageHandler implements MessageHandler<MoveActivityMessage> {

    public static final String AUTHOR_ACTIVITY_MOVE = "author.activity.move";
    static final String AUTHOR_ACTIVITY_MOVE_OK = "author.activity.move.ok";
    static final String AUTHOR_ACTIVITY_MOVE_ERROR = "author.activity.move.error";
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MoveActivityMessageHandler.class);
    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityMovedRTMProducer activityMovedRTMProducer;

    @Inject
    public MoveActivityMessageHandler(final ActivityService activityService,
                                      final CoursewareService coursewareService,
                                      final Provider<AuthenticationContext> authenticationContextProvider,
                                      final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                      final Provider<RTMClientContext> rtmClientContextProvider,
                                      final ActivityMovedRTMProducer activityMovedRTMProducer) {
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityMovedRTMProducer = activityMovedRTMProducer;
    }

    @Override
    public void validate(final MoveActivityMessage message) throws RTMValidationException {

        affirmArgument(message.getActivityId() != null, "activityId is required");
        affirmArgument(message.getPathwayId() != null, "pathwayId is required");
        affirmArgument(activityService.findById(message.getActivityId()).block() != null, "Activity not found");
        affirmArgument(!(message.getIndex() != null && message.getIndex() < 0), "index should be >= 0");
        affirmArgument(activityService.findParentPathwayId(message.getActivityId()).block() != null,
                "Only child activities can be moved. Please use project.activity.move for root activity");

    }

    @Trace(dispatcher = true)
    @Override
    public void handle(final Session session, final MoveActivityMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        Mono<ActivityPayload> activityPayloadMono;

        // do this call blocking cause the unblocking counterpart will make this class too hard to read
        final UUID oldParentPathwayId = activityService.findParentPathwayId(message.getActivityId())
                .block();
        // make sure that the parent pathway is found
        affirmArgument(oldParentPathwayId != null, "invalid root level activity");

        if (message.getIndex() != null) {
            activityPayloadMono = activityService.move(message.getActivityId(),
                    message.getPathwayId(),
                    message.getIndex(),
                    oldParentPathwayId);
        } else {
            activityPayloadMono = activityService.move(message.getActivityId(),
                    message.getPathwayId(),
                    oldParentPathwayId);
        }

        activityPayloadMono
                .doOnEach(log.reactiveErrorThrowable("Error while moving an activity",
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
        Mono.zip(activityPayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ACTIVITY_MOVE_OK,
                                                                                                    message.getId());
                               basicResponseMessage.addField("activity", tuple2.getT1());
                               Responses.writeReactive(session, basicResponseMessage);

                               CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                       .setAccountId(account.getId())
                                       // FIXME set the old pathway id
                                       .setOldParentElement(CoursewareElement.from(oldParentPathwayId, PATHWAY))
                                       .setParentElement(CoursewareElement.from(tuple2.getT1().getParentPathwayId(), PATHWAY))
                                       .setElement(CoursewareElement.from(tuple2.getT1().getActivityId(), ACTIVITY))
                                       .setAction(CoursewareAction.ACTIVITY_MOVED);

                               rtmEventBroker.broadcast(AUTHOR_ACTIVITY_MOVE, broadcastMessage);
                               activityMovedRTMProducer.buildActivityMovedRTMConsumable(rtmClientContext,
                                                                                        tuple2.getT2(),
                                                                                        tuple2.getT1().getActivityId(),
                                                                                        oldParentPathwayId,
                                                                                        tuple2.getT1().getParentPathwayId()).produce();
                           },
                           ex -> {
                               ex = Exceptions.unwrap(ex);
                               String errorMessage = "Unable to move activity";
                               int statusCode = HttpStatus.SC_UNPROCESSABLE_ENTITY;
                               if (ex instanceof IndexOutOfBoundsException) {
                                   errorMessage = "Index is out of range";
                                   statusCode = HttpStatus.SC_BAD_REQUEST;
                               } else if (ex instanceof ActivityNotFoundException) {
                                   errorMessage = "Activity not found";
                                   statusCode = HttpStatus.SC_NOT_FOUND;
                               } else if (ex instanceof ParentPathwayNotFoundException) {
                                   errorMessage = "Parent pathway not found for interactive id";
                                   statusCode = HttpStatus.SC_NOT_FOUND;
                               }
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ACTIVITY_MOVE_ERROR,
                                                       statusCode,
                                                       errorMessage);
                           });
    }

}
