package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
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
import com.smartsparrow.rtm.message.recv.courseware.scenario.CreateScenarioMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ScenarioCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class CreateScenarioMessageHandler implements MessageHandler<CreateScenarioMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateScenarioMessageHandler.class);

    public static final String AUTHOR_SCENARIO_CREATE = "author.scenario.create";
    private static final String AUTHOR_SCENARIO_CREATE_OK = "author.scenario.create.ok";
    private static final String AUTHOR_SCENARIO_CREATE_ERROR = "author.scenario.create.error";

    private final ScenarioService scenarioService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ScenarioCreatedRTMProducer scenarioCreatedRTMProducer;

    @Inject
    CreateScenarioMessageHandler(ScenarioService scenarioService,
                                 CoursewareService coursewareService,
                                 Provider<RTMEventBroker> rtmEventBrokerProvider,
                                 AuthenticationContextProvider authenticationContextProvider,
                                 Provider<RTMClientContext> rtmClientContextProvider,
                                 ScenarioCreatedRTMProducer scenarioCreatedRTMProducer) {
        this.scenarioService = scenarioService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.scenarioCreatedRTMProducer = scenarioCreatedRTMProducer;
    }

    @Override
    public void validate(CreateScenarioMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getLifecycle() != null, "lifecycle is required");
            checkArgument(message.getName() != null, "name is required");
            checkArgument(message.getParentId() != null, "parentId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_SCENARIO_CREATE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_SCENARIO_CREATE)
    @Override
    public void handle(Session session, CreateScenarioMessage message) throws WriteResponseException {
        try {
            String parentType = scenarioService.getScenarioParentTypeByLifecycle(message.getLifecycle());

            RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
            final Account account = authenticationContextProvider.get().getAccount();
            RTMClientContext rtmClientContext = rtmClientContextProvider.get();
            Mono<Scenario> scenarioMono = scenarioService.create(message.getCondition(), message.getActions(),
                    message.getName(), message.getDescription(), message.getCorrectness(),
                    message.getLifecycle(), message.getParentId(), CoursewareElementType.valueOf(parentType))
                    .doOnEach(log.reactiveErrorThrowable("Error occurred while creating a scenario"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .doOnError(scenarioException -> {
                        try {
                            emitError(session, scenarioException.getMessage(), message.getId());
                        } catch (WriteResponseException e) {
                            throw Exceptions.propagate(e);
                        }
                    });
            Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getParentId(), CoursewareElementType.valueOf(parentType));
            Mono.zip(scenarioMono, rootElementIdMono)
                    .subscribe(tuple2 -> {

                        // Emit response to client
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_SCENARIO_CREATE_OK, message.getId());
                        basicResponseMessage.addField("scenario", tuple2.getT1());
                        Responses.writeReactive(session, basicResponseMessage);

                        rtmEventBroker.broadcast(message.getType(), getData(message, parentType, tuple2.getT1(), account));
                        scenarioCreatedRTMProducer.buildScenarioCreatedRTMConsumable(rtmClientContext,
                                                                                     tuple2.getT2(),
                                                                                     tuple2.getT1().getId(),
                                                                                     message.getParentId(),
                                                                                     CoursewareElementType.valueOf(parentType),
                                                                                     tuple2.getT1().getLifecycle()).produce();
                    });

        } catch (Exception e) {
            log.error("Exception while creating scenario", e);
            emitError(session, e.getMessage(), message.getId());
        }
    }

    private CoursewareElementBroadcastMessage getData(CreateScenarioMessage message, String parentType, Scenario scenario,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.CREATED)
                .setScenarioLifecycle(scenario.getLifecycle())
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(scenario.getId(), CoursewareElementType.SCENARIO))
                .setParentElement(CoursewareElement.from(message.getParentId(), Enums.of(CoursewareElementType.class, parentType)));
    }

    private void emitError(Session session, String message, String clientId) throws WriteResponseException {
        ErrorMessage error = new ErrorMessage(AUTHOR_SCENARIO_CREATE_ERROR)
                .setCode(HttpStatus.SC_BAD_REQUEST)
                .setReplyTo(clientId)
                .setMessage(message);
        Responses.write(session, error);
    }
}
