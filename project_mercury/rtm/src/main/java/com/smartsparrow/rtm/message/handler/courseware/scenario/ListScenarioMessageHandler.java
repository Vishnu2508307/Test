package com.smartsparrow.rtm.message.handler.courseware.scenario;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.scenario.ListScenarioMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListScenarioMessageHandler implements MessageHandler<ListScenarioMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListScenarioMessageHandler.class);

    public static final String AUTHOR_SCENARIO_LIST = "author.scenario.list";
    public static final String AUTHOR_SCENARIO_LIST_OK = "author.scenario.list.ok";
    public static final String AUTHOR_SCENARIO_LIST_ERROR = "author.scenario.list.error";

    private final ScenarioService scenarioService;

    @Inject
    public ListScenarioMessageHandler(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @Override
    public void validate(ListScenarioMessage message) throws RTMValidationException {
        if (message.getParentId() == null) {
            throw new RTMValidationException("missing parentId", message.getId(), AUTHOR_SCENARIO_LIST_ERROR);
        }
        if (message.getLifecycle() == null) {
            throw new RTMValidationException("missing lifecycle", message.getId(), AUTHOR_SCENARIO_LIST_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_SCENARIO_LIST)
    @Override
    public void handle(Session session, ListScenarioMessage message) throws WriteResponseException {

        scenarioService.findAll(message.getParentId(), message.getLifecycle())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching list of scenarios"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(scenarios -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_SCENARIO_LIST_OK, message.getId());
                    basicResponseMessage.addField("scenarios", scenarios);
                    Responses.writeReactive(session, basicResponseMessage);
                },  ex -> {
                    log.jsonDebug("Error to fetch list of scenarios", new HashMap<String, Object>(){
                        {
                            put("parentId", message.getParentId());
                            put("lifecycle", message.getLifecycle());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_SCENARIO_LIST_ERROR,
                            HttpStatus.SC_BAD_REQUEST, "error to fetch list of scenarios");
                });
    }
}
