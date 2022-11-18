package com.smartsparrow.rtm.message.handler.courseware.scenario;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioByParent;
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
import com.smartsparrow.rtm.message.recv.courseware.scenario.ReorderScenariosMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.scenarioreordered.ScenarioReOrderedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReorderScenariosMessageHandler implements MessageHandler<ReorderScenariosMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReorderScenariosMessageHandler.class);

    public static final String AUTHOR_SCENARIOS_REORDER = "author.scenarios.reorder";
    private static final String AUTHOR_SCENARIOS_REORDER_OK = "author.scenarios.reorder.ok";
    private static final String AUTHOR_SCENARIOS_REORDER_ERROR = "author.scenarios.reorder.error";

    private final ScenarioService scenarioService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ScenarioReOrderedRTMProducer scenarioReOrderedRTMProducer;

    @Inject
    public ReorderScenariosMessageHandler(ScenarioService scenarioService,
                                          CoursewareService coursewareService,
                                          Provider<RTMEventBroker> rtmEventBrokerProvider,
                                          AuthenticationContextProvider authenticationContextProvider,
                                          Provider<RTMClientContext> rtmClientContextProvider,
                                          ScenarioReOrderedRTMProducer scenarioReOrderedRTMProducer) {
        this.scenarioService = scenarioService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.scenarioReOrderedRTMProducer = scenarioReOrderedRTMProducer;
    }

    @Override
    public void validate(ReorderScenariosMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getParentId() != null, "parentId is required");
            checkArgument(message.getLifecycle() != null, "lifecycle is required");
            checkArgument(message.getScenarioIds() != null, "scenarioIds are required");
            //TODO: match supplied scenarios to the ones already saved
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_SCENARIOS_REORDER_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_SCENARIOS_REORDER)
    public void handle(Session session, ReorderScenariosMessage message) throws WriteResponseException {
        String parentType = scenarioService.getScenarioParentTypeByLifecycle(message.getLifecycle());

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<ScenarioByParent> scenarioByParentMono = scenarioService.reorder(message.getParentId(),
                                                                              message.getLifecycle(),
                                                                              message.getScenarioIds(),
                                                                              CoursewareElementType.valueOf(parentType))
                .doOnEach(log.reactiveErrorThrowable("Error occurred while reordering scenarios"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getParentId(), CoursewareElementType.valueOf(parentType));
        Mono.zip(scenarioByParentMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    // Publish the response
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_SCENARIOS_REORDER_OK, message.getId())
                                                    .addField("scenariosByParent", tuple2.getT1()));

                    rtmEventBroker.broadcast(message.getType(), getData(message, tuple2.getT1(), account));
                    scenarioReOrderedRTMProducer.buildScenarioReOrderedRTMConsumable(rtmClientContext,
                                                                                     tuple2.getT2(),
                                                                                     message.getParentId(),
                                                                                     tuple2.getT1().getParentType(),
                                                                                     message.getScenarioIds(),
                                                                                     message.getLifecycle()).produce();

                }, ex -> {
                    log.jsonDebug("Error to attache scenarios to parent entity", new HashMap<String, Object>() {
                        {
                            put("parentId", message.getParentId());
                            put("lifecycle", message.getLifecycle());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_SCENARIOS_REORDER_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to reorder scenarios");
                });
    }

    private CoursewareElementBroadcastMessage getData(ReorderScenariosMessage message, ScenarioByParent scenarioByParent,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.SCENARIO_REORDERED)
                .setScenarioLifecycle(scenarioByParent.getLifecycle())
                .setAccountId(account.getId())
                .setParentElement(null)
                .setElement(CoursewareElement.from(message.getParentId(), scenarioByParent.getParentType()));
    }
}
