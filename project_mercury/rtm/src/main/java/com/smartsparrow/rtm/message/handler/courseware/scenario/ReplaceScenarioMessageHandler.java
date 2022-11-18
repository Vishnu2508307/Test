package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ParentByScenario;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.scenario.ReplaceScenarioMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.updated.ScenarioUpdatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class ReplaceScenarioMessageHandler implements MessageHandler<ReplaceScenarioMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceScenarioMessageHandler.class);

    public static final String AUTHOR_SCENARIO_REPLACE = "author.scenario.replace";
    private static final String AUTHOR_SCENARIO_REPLACE_OK = "author.scenario.replace.ok";
    private static final String AUTHOR_SCENARIO_REPLACE_ERROR = "author.scenario.replace.error";

    private final ScenarioService scenarioService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ScenarioUpdatedRTMProducer scenarioUpdatedRTMProducer;

    @Inject
    ReplaceScenarioMessageHandler(ScenarioService scenarioService,
                                  CoursewareService coursewareService,
                                  Provider<RTMEventBroker> rtmEventBrokerProvider,
                                  AuthenticationContextProvider authenticationContextProvider,
                                  Provider<RTMClientContext> rtmClientContextProvider,
                                  ScenarioUpdatedRTMProducer scenarioUpdatedRTMProducer
                                  ) {
        this.scenarioService = scenarioService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.scenarioUpdatedRTMProducer = scenarioUpdatedRTMProducer;
    }

    @Override
    public void validate(ReplaceScenarioMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getScenarioId() != null, "scenarioId is required");
            checkArgument(message.getName() != null, "name is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_SCENARIO_REPLACE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_SCENARIO_REPLACE)
    public void handle(Session session, ReplaceScenarioMessage message) throws WriteResponseException {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<Void> scenarioMono = scenarioService.updateScenario (
                message.getScenarioId(),
                message.getCondition(),
                message.getActions(),
                message.getName(),
                message.getDescription(),
                message.getCorrectness())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while replacing a scenario"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnError(scenarioException -> {
                    try {
                        emitError(session, scenarioException.getMessage(), message.getId());
                    } catch (WriteResponseException e) {
                        throw Exceptions.propagate(e);
                    }
                });

        Mono<Scenario> scenario = scenarioService.findById(message.getScenarioId());
        Mono<ParentByScenario> parent = scenarioService.findParent(message.getScenarioId());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getScenarioId(), SCENARIO);
        scenarioMono.then(Mono.zip(scenario, parent, rootElementIdMono))
                .subscribe (
                        tuple3 -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_SCENARIO_REPLACE_OK, message.getId());
                            basicResponseMessage.addField("scenario", tuple3.getT1());
                            Responses.writeReactive(session, basicResponseMessage);

                            rtmEventBroker.broadcast(message.getType(), getData(message, tuple3.getT1(), account));
                            scenarioUpdatedRTMProducer.buildScenarioUpdatedRTMConsumable(rtmClientContext,
                                                                                         tuple3.getT3(),
                                                                                         message.getScenarioId(),
                                                                                         tuple3.getT2().getParentId(),
                                                                                         tuple3.getT2().getParentType(),
                                                                                         tuple3.getT1().getLifecycle()).produce();
                        }
                );

    }

    private CoursewareElementBroadcastMessage getData(ReplaceScenarioMessage message, Scenario scenario, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.UPDATED)
                .setScenarioLifecycle(scenario.getLifecycle())
                .setElement(CoursewareElement.from(message.getScenarioId(), SCENARIO))
                .setParentElement(null);
    }

    private void emitError(Session session, String message, String clientId) throws WriteResponseException {
        ErrorMessage error = new ErrorMessage(AUTHOR_SCENARIO_REPLACE_ERROR)
                .setCode(HttpStatus.SC_BAD_REQUEST)
                .setReplyTo(clientId)
                .setMessage(message);
        Responses.write(session, error);
    }
}
