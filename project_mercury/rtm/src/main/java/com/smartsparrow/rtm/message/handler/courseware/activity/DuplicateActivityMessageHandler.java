package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.DuplicateActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.duplicated.ActivityDuplicatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class DuplicateActivityMessageHandler implements MessageHandler<DuplicateActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DuplicateActivityMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_DUPLICATE = "author.activity.duplicate";
    static final String AUTHOR_ACTIVITY_DUPLICATE_OK = "author.activity.duplicate.ok";
    static final String AUTHOR_ACTIVITY_DUPLICATE_ERROR = "author.activity.duplicate.error";

    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityDuplicatedRTMProducer activityDuplicatedRTMProducer;

    @Inject
    public DuplicateActivityMessageHandler(final ActivityService activityService,
                                           final PathwayService pathwayService,
                                           final CoursewareService coursewareService,
                                           final Provider<AuthenticationContext> authenticationContextProvider,
                                           final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                           final Provider<RTMClientContext> rtmClientContextProvider,
                                           final ActivityDuplicatedRTMProducer activityDuplicatedRTMProducer) {
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityDuplicatedRTMProducer = activityDuplicatedRTMProducer;

    }

    @Override
    public void validate(DuplicateActivityMessage message) throws RTMValidationException {

        if (message.getActivityId() == null) {
            throwError("missing activityId", message.getId());
        }

        if (message.getParentPathwayId() == null) {
            throwError("missing parentPathwayId", message.getId());
        }

        try {
            activityService.findById(message.getActivityId()).block();
        } catch (ActivityNotFoundException e) {
            throwError("activity not found", message.getId());
        }

        try {
            pathwayService.findById(message.getParentPathwayId()).block();
        } catch (PathwayNotFoundException e) {
            throwError("parent pathway not found", message.getId());
        }

        if (message.getIndex() != null && message.getIndex() < 0) {
            throwError("index should be >= 0", message.getId());
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ACTIVITY_DUPLICATE)
    @Override
    public void handle(Session session, DuplicateActivityMessage message) throws WriteResponseException {
        // get feature flag value
        Boolean newDuplicateFlow = message.getNewDuplicateFlow();

        Account account = authenticationContextProvider.get().getAccount();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<Activity> duplicateActivityMono;
        if (message.getIndex() == null) {
            duplicateActivityMono = coursewareService.duplicateActivity(message.getActivityId(),
                    message.getParentPathwayId(), account, newDuplicateFlow)
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext());
        } else {
            duplicateActivityMono = coursewareService.duplicateActivity(message.getActivityId(),
                    message.getParentPathwayId(), message.getIndex(), account, newDuplicateFlow)
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext());
        }

        Mono<ActivityPayload> activityPayloadMono = duplicateActivityMono
                .flatMap(activityService::getActivityPayload)
                .doOnEach(log.reactiveErrorThrowable("error duplicating activity",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .single();
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getParentPathwayId(), PATHWAY);
        Mono.zip(activityPayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ACTIVITY_DUPLICATE_OK,
                                                                                         message.getId());
                    basicResponseMessage.addField("activity", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setAccountId(account.getId())
                            .setParentElement(CoursewareElement.from(message.getParentPathwayId(), PATHWAY))
                            .setElement(CoursewareElement.from(tuple2.getT1().getActivityId(), ACTIVITY))
                            .setAction(CoursewareAction.DUPLICATED);

                    rtmEventBroker.broadcast(AUTHOR_ACTIVITY_DUPLICATE, broadcastMessage);
                    activityDuplicatedRTMProducer.buildActivityDuplicatedRTMConsumable(rtmClientContext,
                                                                                       tuple2.getT2(),
                                                                                       tuple2.getT1().getActivityId(),
                                                                                       message.getParentPathwayId()).produce();
                }, ex -> {
                    ex = Exceptions.unwrap(ex);
                    if (ex instanceof ActivityNotFoundException) {
                        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_DUPLICATE_ERROR,
                                                HttpStatus.SC_NOT_FOUND, "Activity not found");
                    } else {
                        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_DUPLICATE_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to duplicate activity");
                    }
                });
    }

    private static void throwError(String errorMessage, String replyTo) throws RTMValidationException {
        throw new RTMValidationException(errorMessage, replyTo, AUTHOR_ACTIVITY_DUPLICATE_ERROR);
    }
}
