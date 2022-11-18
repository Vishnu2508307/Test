package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.ReplaceInteractiveFeedbackConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.FeedbackConfigChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReplaceInteractiveFeedbackConfigMessageHandler implements MessageHandler<ReplaceInteractiveFeedbackConfigMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceInteractiveFeedbackConfigMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_FEEDBACK_REPLACE = "author.interactive.feedback.replace";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_REPLACE_ERROR = "author.interactive.feedback.replace.error";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_REPLACE_OK = "author.interactive.feedback.replace.ok";

    private final FeedbackService feedbackService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final FeedbackConfigChangeRTMProducer feedbackConfigChangeRTMProducer;

    @Inject
    ReplaceInteractiveFeedbackConfigMessageHandler(FeedbackService feedbackService,
                                                   CoursewareService coursewareService,
                                                   Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                   AuthenticationContextProvider authenticationContextProvider,
                                                   Provider<RTMClientContext> rtmClientContextProvider,
                                                   FeedbackConfigChangeRTMProducer feedbackConfigChangeRTMProducer) {
        this.feedbackService = feedbackService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.feedbackConfigChangeRTMProducer = feedbackConfigChangeRTMProducer;
    }

    @Override
    public void validate(ReplaceInteractiveFeedbackConfigMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getFeedbackId() != null, "feedbackId is required");
            checkArgument(message.getConfig() != null, "config is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_FEEDBACK_REPLACE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_FEEDBACK_REPLACE)
    public void handle(Session session, ReplaceInteractiveFeedbackConfigMessage message)
            throws WriteResponseException {
        if (log.isDebugEnabled()) {
            log.debug("Processing ReplaceInteractiveFeedbackConfigMessage: " + message);
        }

        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        Mono<FeedbackConfig> feedbackConfigMono = feedbackService.replace(message.getFeedbackId(), message.getConfig())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while replacing feedback config"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getFeedbackId(), FEEDBACK);
        Mono.zip(feedbackConfigMono, rootElementIdMono).subscribe(
                tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                            AUTHOR_INTERACTIVE_FEEDBACK_REPLACE_OK,
                            message.getId());
                    basicResponseMessage.addField("feedbackConfig", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setElement(CoursewareElement.from(message.getFeedbackId(), FEEDBACK))
                            .setAccountId(account.getId())
                            .setAction(CoursewareAction.CONFIG_CHANGE);
                    rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_FEEDBACK_REPLACE, broadcastMessage);
                    feedbackConfigChangeRTMProducer.buildFeedbackConfigChangeRTMConsumable(rtmClientContext,
                                                                                           tuple2.getT2(),
                                                                                           message.getFeedbackId(),
                                                                                           message.getConfig()).produce();
                },
                ex -> {
                    if (ex instanceof FeedbackNotFoundException) {
                        emitError(session, ex.getMessage(), message.getId(), HttpStatus.SC_NOT_FOUND);
                    } else {
                        emitError(session,
                                  "Unable to replace feedback config",
                                  message.getId(),
                                  HttpStatus.SC_UNPROCESSABLE_ENTITY);
                    }
                });
    }

    private void emitError(Session session, String message, String messageId, int code) {
        ErrorMessage error = new ErrorMessage(AUTHOR_INTERACTIVE_FEEDBACK_REPLACE_ERROR);
        error.setReplyTo(messageId);
        error.setCode(code);
        error.setMessage(message);
        Responses.writeReactive(session, error);
    }
}
