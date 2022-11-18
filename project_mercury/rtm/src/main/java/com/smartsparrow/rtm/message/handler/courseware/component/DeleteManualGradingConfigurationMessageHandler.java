package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.util.Warrants.affirmArgument;

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
import com.smartsparrow.rtm.message.recv.courseware.component.ComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.manualgrading.ComponentManualGradingConfigDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteManualGradingConfigurationMessageHandler implements MessageHandler<ComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteManualGradingConfigurationMessageHandler.class);

    public static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE = "author.component.manual.grading.configuration.delete";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE_OK = "author.component.manual.grading.configuration.delete.ok";
    private static final String AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE_ERROR = "author.component.manual.grading.configuration.delete.error";

    private final ComponentService componentService;
    private final ManualGradeService manualGradeService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentManualGradingConfigDeletedRTMProducer componentManualGradingConfigDeletedRTMProducer;

    @Inject
    public DeleteManualGradingConfigurationMessageHandler(ComponentService componentService,
                                                          ManualGradeService manualGradeService,
                                                          Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                          AuthenticationContextProvider authenticationContextProvider,
                                                          CoursewareService coursewareService,
                                                          Provider<RTMClientContext> rtmClientContextProvider,
                                                          ComponentManualGradingConfigDeletedRTMProducer componentManualGradingConfigDeletedRTMProducer) {
        this.componentService = componentService;
        this.manualGradeService = manualGradeService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentManualGradingConfigDeletedRTMProducer = componentManualGradingConfigDeletedRTMProducer;
    }

    @Override
    public void validate(ComponentMessage message) throws RTMValidationException {
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

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, ComponentMessage message) throws WriteResponseException {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        manualGradeService.deleteManualGradingConfiguration(message.getComponentId())
                .next()
                .doOnEach(log.reactiveErrorThrowable("Error while deleting manual grading configuration"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getComponentId(), COMPONENT))
                .subscribe(rootElementId -> {
                    componentManualGradingConfigDeletedRTMProducer.buildManualGradingConfigDeletedRTMConsumable(
                            rtmClientContext,
                            rootElementId,
                            message.getComponentId()).produce();
                }, ex -> {
                    Responses.errorReactive(session,
                                            message.getId(),
                                            AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            ex.getMessage());
                }, () -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(
                                                    AUTHOR_COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETE_OK,
                                                    message.getId())
                                                    .addField("manualGradingConfiguration",
                                                              new ManualGradingConfiguration()
                                                                      .setComponentId(message.getComponentId())));
                    rtmEventBroker.broadcast(message.getType(), getData(message, account));
                });
    }

    private CoursewareElementBroadcastMessage getData(ComponentMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setElement(CoursewareElement.from(message.getComponentId(), COMPONENT))
                .setAccountId(account.getId())
                .setAction(CoursewareAction.MANUAL_GRADING_CONFIGURATION_DELETED);
    }
}
