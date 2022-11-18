package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.handler.courseware.activity.GetActivityMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.interactive.GetInteractiveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;

public class GetInteractiveMessageHandler implements MessageHandler<GetInteractiveMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(GetActivityMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_GET = "author.interactive.get";
    public static final String AUTHOR_INTERACTIVE_GET_OK = "author.interactive.get.ok";
    public static final String AUTHOR_INTERACTIVE_GET_ERROR = "author.interactive.get.error";

    private final InteractiveService interactiveService;

    @Inject
    public GetInteractiveMessageHandler(InteractiveService interactiveService) {
        this.interactiveService = interactiveService;
    }

    @Override
    public void validate(GetInteractiveMessage message) throws RTMValidationException {
        affirmArgument(message.getInteractiveId() != null, "interactiveId is missing");
        affirmArgument(interactiveService.findById(message.getInteractiveId()).block() != null, "Interactive not found");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_GET)
    public void handle(Session session, GetInteractiveMessage message) {
        interactiveService.getInteractivePayload(message.getInteractiveId())
                .doOnEach(logger.reactiveErrorThrowable("Returned values from interactiveService.getInteractivePayload ",
                                                        throwable -> new HashMap<String, Object>() {
                                                            {
                                                                put("message", message.toString());
                                                            }
                                                        }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(payload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_GET_OK, message.getId());
                    basicResponseMessage.addField("interactive", payload);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    ex = Exceptions.unwrap(ex);
                    int status = ex instanceof InteractiveNotFoundException ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_UNPROCESSABLE_ENTITY;
                    logger.jsonDebug("Can't get interactive with id", new HashMap<String, Object>() {
                        {
                            put("interactiveId", message.getInteractiveId());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_GET_ERROR, status,
                            ex.getMessage());
                });
    }
}
