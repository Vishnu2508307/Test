package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.ManualGradingConfigurationSetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentConfigurationCreatedRTMProducer;
import com.smartsparrow.rtm.subscription.courseware.message.ManualGradingConfig;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class SetManualGradeConfigurationMessageHandler implements MessageHandler<ManualGradingConfigurationSetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SetManualGradeConfigurationMessageHandler.class);

    public static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET = "author.component.manual.grading.configuration.set";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET_OK = "author.component.manual.grading.configuration.set.ok";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET_ERROR = "author.component.manual.grading.configuration.set.error";

    private final ComponentService componentService;
    private final ManualGradeService manualGradeService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentConfigurationCreatedRTMProducer componentConfigurationCreatedRTMProducer;

    @Inject
    public SetManualGradeConfigurationMessageHandler(ComponentService componentService,
                                                     ManualGradeService manualGradeService,
                                                     Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                     AuthenticationContextProvider authenticationContextProvider,
                                                     CoursewareService coursewareService,
                                                     Provider<RTMClientContext> rtmClientContextProvider,
                                                     ComponentConfigurationCreatedRTMProducer componentConfigurationCreatedRTMProducer) {
        this.componentService = componentService;
        this.manualGradeService = manualGradeService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentConfigurationCreatedRTMProducer = componentConfigurationCreatedRTMProducer;
    }

    @Override
    public void validate(ManualGradingConfigurationSetMessage message) throws RTMValidationException {
        // check that the required arguments are supplied correctly
        affirmArgument(message.getComponentId() != null, "componentId is required");

        // throw a fault if the component does not exist
        componentService.findById(message.getComponentId())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching component"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .doOnError(ComponentNotFoundException.class, ex -> {
                    throw new IllegalArgumentFault(ex.getMessage());
                })
                .block();
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET)
    public void handle(Session session, ManualGradingConfigurationSetMessage message) throws WriteResponseException {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<ManualGradingConfiguration> manualGradingConfigurationMono = manualGradeService.createManualGradingConfiguration(
                        message.getComponentId(),
                        message.getMaxScore())
                .doOnEach(log.reactiveErrorThrowable("Error while creating manual grading configuration"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getComponentId(), COMPONENT);
        Mono.zip(manualGradingConfigurationMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(
                                                    AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET_OK,
                                                    message.getId())
                                                    .addField("manualGradingConfiguration", tuple2.getT1()));
                    rtmEventBroker.broadcast(message.getType(), getData(message, account));

                    componentConfigurationCreatedRTMProducer.buildComponentConfigurationCreatedRTMConsumable(
                            rtmClientContext,
                            tuple2.getT2(),
                            message.getComponentId(),
                            ManualGradingConfig.from(tuple2.getT1())).produce();

                }, ex -> {
                    Responses.errorReactive(session,
                                            message.getId(),
                                            AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_SET_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            ex.getMessage());
                });
    }

    private CoursewareElementBroadcastMessage getData(ManualGradingConfigurationSetMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setElement(CoursewareElement.from(message.getComponentId(), COMPONENT))
                .setAccountId(account.getId())
                .setAction(CoursewareAction.MANUAL_GRADING_CONFIGURATION_CREATED);
    }
}
