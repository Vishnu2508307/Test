package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.ReplaceActivityThemeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.themechange.ActivityThemeChangeRTMProducer;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class ReplaceActivityThemeMessageHandler implements MessageHandler<ReplaceActivityThemeMessage> {

    public static final String AUTHOR_ACTIVITY_THEME_REPLACE = "author.activity.theme.replace";
    private static final String AUTHOR_ACTIVITY_THEME_REPLACE_OK = "author.activity.theme.replace.ok";
    private static final String AUTHOR_ACTIVITY_THEME_REPLACE_ERROR = "author.activity.theme.replace.error";

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceActivityThemeMessageHandler.class);

    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityThemeChangeRTMProducer activityThemeChangeRTMProducer;

    @Inject
    ReplaceActivityThemeMessageHandler(ActivityService activityService,
                                       CoursewareService coursewareService,
                                       Provider<RTMEventBroker> rtmEventBrokerProvider,
                                       AuthenticationContextProvider authenticationContextProvider,
                                       Provider<RTMClientContext> rtmClientContextProvider,
                                       ActivityThemeChangeRTMProducer activityThemeChangeRTMProducer) {
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityThemeChangeRTMProducer = activityThemeChangeRTMProducer;
    }

    @Override
    public void validate(ReplaceActivityThemeMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getActivityId() != null, "activity id is required");
        } catch (IllegalArgumentException e) {
            log.error("Exception while replacing an activity theme {}", e.getMessage());
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ACTIVITY_THEME_REPLACE_ERROR);
        }
    }

    /**
     * Handle the incoming message. Replace the activity theme config and publish an event when the theme config is
     * successfully replaced.
     *
     * @param session the web socket session
     * @param message the newly arrived message
     * @throws WriteResponseException when failing to write on the web socket
     */
    @Trace(dispatcher = true, nameTransaction = true, metricName = AUTHOR_ACTIVITY_THEME_REPLACE)
    @Override
    public void handle(Session session, ReplaceActivityThemeMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ACCOUNT_ID.getValue(), account.getId().toString(), log);

        Mono<ActivityTheme> activityThemeMono = activityService.replaceActivityThemeConfig(
                        message.getActivityId(),
                        message.getConfig())
                .doOnEach(log.reactiveErrorThrowable("error while replacing theme for activity",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getActivityId(), ACTIVITY);
        Mono.zip(activityThemeMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                               emitResponse(session, message, tuple2.getT1());
                               rtmEventBroker
                                       .broadcast(message.getType(), getData(message, account));
                               activityThemeChangeRTMProducer.buildActivityThemeChangeRTMConsumable(rtmClientContext,
                                                                                                    tuple2.getT2(),
                                                                                                    message.getActivityId(),
                                                                                                    message.getConfig()).produce();
                           },
                           ex -> {
                               emitError(session, message, ex);
                           });

    }

    /**
     * Build the data that the {@link RTMEventBroker} will send with the event message.
     *
     * @param message the incoming message
     * @param account the account performing the action
     * @return the data
     */
    private CoursewareElementBroadcastMessage getData(ReplaceActivityThemeMessage message, Account account) {

        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.THEME_CHANGE)
                .setAccountId(account.getId())
                .setParentElement(null)
                .setElement(CoursewareElement.from(message.getActivityId(), CoursewareElementType.ACTIVITY));
    }

    private void emitResponse(Session session, ReplaceActivityThemeMessage message, ActivityTheme activityTheme) {
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ACTIVITY_THEME_REPLACE_OK,
                message.getId());
        basicResponseMessage.addField("activityTheme", activityTheme);
        Responses.writeReactive(session, basicResponseMessage);
    }

    private void emitError(Session session, ReplaceActivityThemeMessage message, Throwable ex) {
        ex = Exceptions.unwrap(ex);
        Throwable finalEx = ex;
        log.error("Failed to replace Activity theme", new HashMap<String, Object>() {
            {
                put("activityId", message.getActivityId());
                put("config", message.getConfig());
                put("error", finalEx.getStackTrace());
            }
        });
        String errorMessage = "Unable to replace activity";
        int code = HttpStatus.SC_BAD_REQUEST;
        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_THEME_REPLACE_ERROR, code, errorMessage);
    }
}
