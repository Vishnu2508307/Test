package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.ReplaceComponentConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.ComponentConfigChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReplaceComponentConfigMessageHandler implements MessageHandler<ReplaceComponentConfigMessage> {

    private static MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceComponentConfigMessageHandler.class);

    public final static String AUTHOR_COMPONENT_REPLACE = "author.component.replace";
    private final static String AUTHOR_COMPONENT_REPLACE_OK = "author.component.replace.ok";
    private final static String AUTHOR_COMPONENT_REPLACE_ERROR = "author.component.replace.error";

    private final ComponentService componentService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentConfigChangeRTMProducer componentConfigChangeRTMProducer;

    @Inject
    ReplaceComponentConfigMessageHandler(ComponentService componentService,
                                         Provider<RTMEventBroker> rtmEventBrokerProvider,
                                         CoursewareService coursewareService,
                                         AuthenticationContextProvider authenticationContextProvider,
                                         Provider<RTMClientContext> rtmClientContextProvider,
                                         ComponentConfigChangeRTMProducer componentConfigChangeRTMProducer) {
        this.componentService = componentService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentConfigChangeRTMProducer = componentConfigChangeRTMProducer;
    }

    @Override
    public void validate(ReplaceComponentConfigMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getComponentId() != null, "componentId is missing");
            checkArgument(StringUtils.isNotBlank(message.getConfig()), "config is missing");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_COMPONENT_REPLACE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COMPONENT_REPLACE)
    @Override
    public void handle(Session session, ReplaceComponentConfigMessage message) throws WriteResponseException {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<ComponentConfig> componentConfigMono = componentService.replaceConfig(message.getComponentId(), message.getConfig())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while replacing component config"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                // extract the configuration fields
                .doOnNext(componentConfig -> coursewareService
                        .saveConfigurationFields(message.getComponentId(), message.getConfig())
                        .subscribe()
                )
                .doOnError(componentException -> {
                    emitError(session, componentException.getMessage(), message.getId());
                });
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getComponentId(), COMPONENT);
        Mono.zip(componentConfigMono, rootElementIdMono)
                .subscribe(
                        tuple2 -> {
                            BasicResponseMessage basicResponseMessage =
                                    new BasicResponseMessage(AUTHOR_COMPONENT_REPLACE_OK, message.getId());
                            basicResponseMessage.addField("componentConfig", tuple2.getT1());
                            Responses.writeReactive(session, basicResponseMessage);

                            CoursewareElementBroadcastMessage broadcastMessage = getData(message, account);

                            rtmEventBroker.broadcast(message.getType(), broadcastMessage);
                            componentConfigChangeRTMProducer.buildComponentConfigChangeRTMConsumable(rtmClientContext,
                                                                                                     tuple2.getT2(),
                                                                                                     message.getComponentId(),
                                                                                                     message.getConfig()).produce();
                        }
                );

    }

    private CoursewareElementBroadcastMessage getData(ReplaceComponentConfigMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.CONFIG_CHANGE)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(message.getComponentId(), COMPONENT))
                .setParentElement(null);
    }

    private void emitError(Session session, String message, String clientId) {
        ErrorMessage error = new ErrorMessage(AUTHOR_COMPONENT_REPLACE_ERROR)
                .setCode(HttpStatus.SC_BAD_REQUEST)
                .setReplyTo(clientId)
                .setMessage(message);
        Responses.writeReactive(session, error);
    }
}
