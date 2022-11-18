package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.smartsparrow.rtm.message.recv.courseware.activity.ReplaceActivityConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.ActivityConfigChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class ReplaceActivityConfigMessageHandler implements MessageHandler<ReplaceActivityConfigMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceActivityConfigMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_CONFIG_REPLACE = "author.activity.config.replace";
    private static final String AUTHOR_ACTIVITY_CONFIG_REPLACE_OK = "author.activity.config.replace.ok";
    static final String AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR = "author.activity.config.replace.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final ActivityService activityService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityConfigChangeRTMProducer activityConfigChangeRTMProducer;

    @Inject
    public ReplaceActivityConfigMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                               ActivityService activityService,
                                               Provider<RTMEventBroker> rtmEventBrokerProvider,
                                               CoursewareService coursewareService,
                                               Provider<RTMClientContext> rtmClientContextProvider,
                                               ActivityConfigChangeRTMProducer activityConfigChangeRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.activityService = activityService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityConfigChangeRTMProducer = activityConfigChangeRTMProducer;
    }

    @Override
    public void validate(ReplaceActivityConfigMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getActivityId() != null, "missing activityId");
            checkArgument(message.getConfig() != null, "missing config");
            activityService.findById(message.getActivityId()).block();

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR);
        } catch (ActivityNotFoundException ex) {
            throw new RTMValidationException("invalid activityId", message.getId(), AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR);
        }
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, ReplaceActivityConfigMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        List<ActivityPayload> payload = new ArrayList<>(1);

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        // replace the activity configuration with the supplied version.
        Mono<ActivityPayload> activityPayloadMono = activityService.replaceConfig(account.getId(),
                                                                                  message.getActivityId(),
                                                                                  message.getConfig())
                .thenMany(coursewareService.saveConfigurationFields(message.getActivityId(), message.getConfig()))
                .then(activityService.getActivityPayload(message.getActivityId()))
                .doOnEach(log.reactiveErrorThrowable("error replacing activity config ",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                             put("config", message.getConfig());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getActivityId(), ACTIVITY);
        Mono.zip(activityPayloadMono, rootElementIdMono).subscribe(
                //onNext
                tuple2 -> {
                    // Add to the onComplete collection
                    payload.add(tuple2.getT1());

                    // publish event to pub/sub listeners
                    rtmEventBroker.broadcast(message.getType(), getData(message, account));
                    activityConfigChangeRTMProducer.buildActivityConfigChangeRTMConsumable(rtmClientContext,
                                                                                           tuple2.getT2(),
                                                                                           message.getActivityId(),
                                                                                           message.getConfig()).produce();
                },
                //onError
                ex -> {
                    emitError(session, message, ex);
                },
                //onComplete
                () -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                            AUTHOR_ACTIVITY_CONFIG_REPLACE_OK,
                            message.getId());
                    if (!payload.isEmpty()) {
                        basicResponseMessage.addField("activity", payload.get(0));
                    } else {
                        log.warn("For some reason payload was not fetched for activityId " + message.getActivityId());
                        basicResponseMessage.setCode(HttpStatus.SC_ACCEPTED);
                    }
                    Responses.writeReactive(session, basicResponseMessage);
                });
    }

    private void emitError(Session session, ReplaceActivityConfigMessage message, Throwable ex) {

        ex = Exceptions.unwrap(ex);

        log.warn(ex.getMessage());

        String errorMessage = "Unable to replace config";

        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_CONFIG_REPLACE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, errorMessage);
    }

    /**
     * Build the data that the {@link RTMEventBroker} will send with the event message.
     *
     * @param message the incoming message
     * @return the data
     */
    private CoursewareElementBroadcastMessage getData(ReplaceActivityConfigMessage message, Account account) {

        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.CONFIG_CHANGE)
                .setElement(CoursewareElement.from(message.getActivityId(), CoursewareElementType.ACTIVITY))
                .setAccountId(account.getId())
                .setParentElement(null);
    }

}

