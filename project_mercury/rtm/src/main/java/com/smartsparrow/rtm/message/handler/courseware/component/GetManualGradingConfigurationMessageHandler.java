package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetManualGradingConfigurationMessageHandler implements MessageHandler<ComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetManualGradingConfigurationMessageHandler.class);

    public static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET = "author.component.manual.grading.configuration.get";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET_OK = "author.component.manual.grading.configuration.get.ok";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET_ERROR = "author.component.manual.grading.configuration.get.error";

    private final ComponentService componentService;

    @Inject
    public GetManualGradingConfigurationMessageHandler(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Override
    public void validate(ComponentMessage message) throws RTMValidationException {
        affirmArgument(message.getComponentId() != null, "componentId is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET)
    public void handle(Session session, ComponentMessage message) throws WriteResponseException {
        componentService.findManualGradingConfiguration(message.getComponentId())
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching manual grading configuration"))
                .defaultIfEmpty(new ManualGradingConfiguration())  // return an empty object if not found
                .subscribe(found -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET_OK,
                            message.getId())
                            .addField("manualGradingConfiguration", found));
                }, ex -> {
                    Responses.errorReactive(session,
                            message.getId(),
                            AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_GET_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            ex.getMessage());
                });
    }
}
