package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.payload.FeedbackPayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.CreateInteractiveFeedbackMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.created.FeedbackCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class CreateInteractiveFeedbackMessageHandler implements MessageHandler<CreateInteractiveFeedbackMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateInteractiveFeedbackMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_FEEDBACK_CREATE = "author.interactive.feedback.create";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_CREATE_ERROR = "author.interactive.feedback.create.error";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_CREATE_OK = "author.interactive.feedback.create.ok";

    private final FeedbackService feedbackService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final FeedbackCreatedRTMProducer feedbackCreatedRTMProducer;

    @Inject
    CreateInteractiveFeedbackMessageHandler(FeedbackService feedbackService,
                                            CoursewareService coursewareService,
                                            Provider<RTMEventBroker> rtmEventBrokerProvider,
                                            AuthenticationContextProvider authenticationContextProvider,
                                            Provider<RTMClientContext> rtmClientContextProvider,
                                            FeedbackCreatedRTMProducer feedbackCreatedRTMProducer) {
        this.feedbackService = feedbackService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.feedbackCreatedRTMProducer = feedbackCreatedRTMProducer;
    }

    @Override
    public void validate(CreateInteractiveFeedbackMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getInteractiveId() != null, "interactiveId is required");
            checkArgument(message.getPluginId() != null, "plugin id is required");
            checkArgument(StringUtils.isNotBlank(message.getPluginVersionExp()), "plugin version is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_FEEDBACK_CREATE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_FEEDBACK_CREATE)
    public void handle(Session session, CreateInteractiveFeedbackMessage message) throws WriteResponseException {
        if (log.isDebugEnabled()) {
            log.debug("Processing CreateInteractiveFeedbackMessage: " + message);
        }

        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        Mono<Feedback> feedbackMono = feedbackService.create(message.getInteractiveId(),
                                                             message.getPluginId(),
                                                             message.getPluginVersionExp())
                .doOnEach(log.reactiveErrorThrowable("Error while creating interactive feedback with plugin"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion monitoring
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnError(exception -> Responses.errorReactive(session,
                                                                message.getId(),
                                                                AUTHOR_INTERACTIVE_FEEDBACK_CREATE_ERROR,
                                                                HttpStatus.SC_BAD_REQUEST,
                                                                exception.getMessage())
                );

        Mono<FeedbackPayload> feedbackPayloadMono = feedbackMono.flatMap(feedbackService::getFeedbackPayload);
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE);
        Mono.zip(feedbackPayloadMono, rootElementIdMono).subscribe(tuple2 -> {
            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                    AUTHOR_INTERACTIVE_FEEDBACK_CREATE_OK, message.getId());
            basicResponseMessage.addField("feedback", tuple2.getT1());
            Responses.writeReactive(session, basicResponseMessage);

            CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                    .setParentElement(CoursewareElement.from(tuple2.getT1().getInteractiveId(), INTERACTIVE))
                    .setElement(CoursewareElement.from(tuple2.getT1().getFeedbackId(), FEEDBACK))
                    .setAccountId(account.getId())
                    .setAction(CoursewareAction.CREATED);
            rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_FEEDBACK_CREATE, broadcastMessage);
            feedbackCreatedRTMProducer.buildFeedbackCreatedRTMConsumable(rtmClientContext,
                                                                         tuple2.getT2(),
                                                                         tuple2.getT1().getFeedbackId()).produce();
        });
    }
}
