package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.interactive.InteractiveScenariosTestMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class InteractiveScenariosTestMessageHandler implements MessageHandler<InteractiveScenariosTestMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(InteractiveScenariosTestMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_SCENARIOS_TEST = "author.interactive.scenarios.test";
    private static final String AUTHOR_INTERACTIVE_SCENARIOS_TEST_OK = "author.interactive.scenarios.test.ok";
    private static final String AUTHOR_INTERACTIVE_SCENARIOS_TEST_ERROR = "author.interactive.scenarios.test.error";

    private final WalkableService walkableService;

    @Inject
    public InteractiveScenariosTestMessageHandler(final WalkableService walkableService) {
        this.walkableService = walkableService;
    }

    @Override
    public void validate(InteractiveScenariosTestMessage message) throws RTMValidationException {
        affirmArgument(message.getInteractiveId() != null, "interactiveId is required");
        affirmArgumentNotNullOrEmpty(message.getScopeData(), "scopeData is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_SCENARIOS_TEST)
    public void handle(Session session, InteractiveScenariosTestMessage message) throws WriteResponseException {
        // perform the test evaluation
        walkableService.evaluate(message.getInteractiveId(), CoursewareElementType.INTERACTIVE, message.getScopeData())
                .doOnEach(log.reactiveErrorThrowable("Evaluate Scenario", throwable -> new HashMap<String, Object>() {
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
                // reply to the client
                .subscribe(testEvaluationResponse -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_INTERACTIVE_SCENARIOS_TEST_OK,
                            message.getId())
                            .addField("results", testEvaluationResponse.getScenarioEvaluationResults()));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_SCENARIOS_TEST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not evaluate test scenarios");
                });
    }
}
