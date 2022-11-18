package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.handler.cohort.GetCohortMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetComponentMessageHandler implements MessageHandler<ComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCohortMessageHandler.class);

    public static final String AUTHOR_COMPONENT_GET = "author.component.get";
    private static final String AUTHOR_COMPONENT_GET_OK = "author.component.get.ok";
    private static final String AUTHOR_COMPONENT_GET_ERROR = "author.component.get.error";

    private final ComponentService componentService;

    @Inject
    public GetComponentMessageHandler(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Override
    public void validate(ComponentMessage message) throws RTMValidationException {
        affirmArgument(message.getComponentId() != null, "componentId is missing");
        affirmArgument(componentService.findById(message.getComponentId()).block() != null, "Component not found");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COMPONENT_GET)
    public void handle(Session session, ComponentMessage message) throws WriteResponseException {
        componentService.getComponentPayload(message.getComponentId())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching component payload"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(payload ->
                        emitSuccess(session, message, payload),
                        ex -> emitError(session, message, ex));
    }

    public void emitError(Session session, ComponentMessage message, Throwable ex) {
        String error = String.format("error fetching component %s", message.getComponentId());
        Responses.errorReactive(session, message.getId(), AUTHOR_COMPONENT_GET_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY, error);
    }

    public void emitSuccess(Session session, ComponentMessage message, ComponentPayload payload) {
        BasicResponseMessage response = new BasicResponseMessage(AUTHOR_COMPONENT_GET_OK, message.getId())
                .addField("component", payload);
        Responses.writeReactive(session, response);
    }
}
