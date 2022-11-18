package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.ParentInteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.feedback.DeleteInteractiveFeedbackMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.FeedbackDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteInteractiveFeedbackMessageHandler implements MessageHandler<DeleteInteractiveFeedbackMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteInteractiveFeedbackMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_FEEDBACK_DELETE = "author.interactive.feedback.delete";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_DELETE_OK = "author.interactive.feedback.delete.ok";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR = "author.interactive.feedback.delete.error";

    private final FeedbackService feedbackService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final FeedbackDeletedRTMProducer feedbackDeletedRTMProducer;

    @Inject
    public DeleteInteractiveFeedbackMessageHandler(FeedbackService feedbackService,
                                                   CoursewareService coursewareService,
                                                   Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                   AuthenticationContextProvider authenticationContextProvider,
                                                   Provider<RTMClientContext> rtmClientContextProvider,
                                                   FeedbackDeletedRTMProducer feedbackDeletedRTMProducer) {
        this.feedbackService = feedbackService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.feedbackDeletedRTMProducer = feedbackDeletedRTMProducer;
    }

    @Override
    public void validate(DeleteInteractiveFeedbackMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getFeedbackId() != null, "feedbackId is required");
            checkArgument(message.getInteractiveId() != null, "interactiveId is required");
            feedbackService.findById(message.getFeedbackId()).block();

            UUID interactiveId = feedbackService.findParentId(message.getFeedbackId())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching interactive ID for feedback"))
                    .block();

            checkArgument(message.getInteractiveId().equals(interactiveId),
                    "supplied interactiveId does not match the feedback parent");
        } catch (IllegalArgumentException | FeedbackNotFoundException | ParentInteractiveNotFoundException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_FEEDBACK_DELETE)
    public void handle(Session session, DeleteInteractiveFeedbackMessage message) {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        feedbackService.delete(message.getFeedbackId(), message.getInteractiveId())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while deleting feedback parent relationships"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE))
                .subscribe(rootElementId -> {
                               feedbackDeletedRTMProducer.buildFeedbackDeletedRTMConsumable(rtmClientContext,
                                                                                            rootElementId,
                                                                                            message.getFeedbackId()).produce();
                           },
                           ex -> {
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_INTERACTIVE_FEEDBACK_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       ex.getMessage());
                           },
                           () -> {
                               BasicResponseMessage basicResponseMessage =
                                       new BasicResponseMessage(
                                               AUTHOR_INTERACTIVE_FEEDBACK_DELETE_OK,
                                               message.getId());
                               basicResponseMessage.addField("feedbackId",
                                                             message.getFeedbackId());
                               basicResponseMessage.addField("interactiveId",
                                                             message.getInteractiveId());
                               Responses.writeReactive(session, basicResponseMessage);

                               CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                       .setParentElement(CoursewareElement.from(message.getInteractiveId(),
                                                                                INTERACTIVE))
                                       .setElement(CoursewareElement.from(message.getFeedbackId(),
                                                                          FEEDBACK))
                                       .setAction(CoursewareAction.DELETED)
                                       .setAccountId(account.getId());
                               rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_FEEDBACK_DELETE,
                                                        broadcastMessage);
                           });
    }
}
