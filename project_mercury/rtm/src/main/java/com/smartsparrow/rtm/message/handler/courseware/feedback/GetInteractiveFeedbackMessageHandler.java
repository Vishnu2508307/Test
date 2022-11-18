package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.feedback.GetInteractiveFeedbackMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class GetInteractiveFeedbackMessageHandler implements MessageHandler<GetInteractiveFeedbackMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetInteractiveFeedbackMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_FEEDBACK_GET = "author.interactive.feedback.get";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_GET_OK = "author.interactive.feedback.get.ok";
    public static final String AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR = "author.interactive.feedback.get.error";

    private final FeedbackService feedbackService;

    @Inject
    public GetInteractiveFeedbackMessageHandler(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @Override
    public void validate(GetInteractiveFeedbackMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getFeedbackId() != null, "feedbackId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_FEEDBACK_GET)
    public void handle(Session session, GetInteractiveFeedbackMessage message) {
        feedbackService.getFeedbackPayload(message.getFeedbackId())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching feedback payload"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(
                        payload -> {
                            BasicResponseMessage basicResponseMessage =
                                    new BasicResponseMessage(AUTHOR_INTERACTIVE_FEEDBACK_GET_OK, message.getId());
                            basicResponseMessage.addField("feedback", payload);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> {
                            log.debug("Unable to fetch feedback payload", new HashMap<String, Object>(){
                                {
                                    put("feedbackId", message.getFeedbackId());
                                    put("error", ex.getStackTrace());
                                }
                            });

                            Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_FEEDBACK_GET_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY, ex.getMessage());
                        });
    }
}
