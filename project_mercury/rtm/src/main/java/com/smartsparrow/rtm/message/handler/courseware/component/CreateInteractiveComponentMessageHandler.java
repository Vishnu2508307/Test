package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ComponentAlreadyExistsFault;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.CreateInteractiveComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class CreateInteractiveComponentMessageHandler implements MessageHandler<CreateInteractiveComponentMessage> {

    public final static String AUTHOR_INTERACTIVE_COMPONENT_CREATE = "author.interactive.component.create";
    private final static String AUTHOR_INTERACTIVE_COMPONENT_CREATE_OK = "author.interactive.component.create.ok";
    final static String AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR = "author.interactive.component.create.error";

    private final ComponentService componentService;
    private final InteractiveService interactiveService;
    private final PluginService pluginService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentCreatedRTMProducer componentCreatedRTMProducer;

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateInteractiveComponentMessageHandler.class);

    @Inject
    public CreateInteractiveComponentMessageHandler(ComponentService componentService,
                                                    InteractiveService interactiveService,
                                                    PluginService pluginService,
                                                    Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                    CoursewareService coursewareService,
                                                    AuthenticationContextProvider authenticationContextProvider,
                                                    Provider<RTMClientContext> rtmClientContextProvider,
                                                    ComponentCreatedRTMProducer componentCreatedRTMProducer) {
        this.componentService = componentService;
        this.interactiveService = interactiveService;
        this.pluginService = pluginService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentCreatedRTMProducer = componentCreatedRTMProducer;
    }

    @Override
    public void validate(CreateInteractiveComponentMessage message) throws RTMValidationException {
        try {

            checkArgument(message.getInteractiveId() != null, "interactiveId is required");
            checkArgument(message.getPluginId() != null, "pluginId is required");
            checkArgument(StringUtils.isNotBlank(message.getPluginVersionExpr()), "pluginVersion is required");

            interactiveService.findById(message.getInteractiveId())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching interactive"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

            pluginService.findLatestVersion(message.getPluginId(), message.getPluginVersionExpr())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching latest plugin version"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR);
        } catch (InteractiveNotFoundException e) {
            throw new RTMValidationException("invalid interactive", message.getId(), AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR);
        } catch (VersionParserFault e) {
            throw new RTMValidationException("invalid pluginVersion", message.getId(), AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR);
        } catch (PluginNotFoundFault e) {
            throw new RTMValidationException("invalid plugin", message.getId(), AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_COMPONENT_CREATE)
    @Override
    public void handle(Session session, CreateInteractiveComponentMessage message) throws WriteResponseException {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<Component> componentMono;

        if (message.getComponentId() != null) {
            componentMono = componentService.createForInteractive(
                    message.getInteractiveId(),
                    message.getPluginId(),
                    message.getPluginVersionExpr(),
                    message.getConfig(),
                    message.getComponentId());
        } else {
            componentMono = componentService.createForInteractive(
                    message.getInteractiveId(),
                    message.getPluginId(),
                    message.getPluginVersionExpr(),
                    message.getConfig());
        }

        Mono<ComponentPayload> componentPayloadMono = componentMono
                .doOnEach(log.reactiveErrorThrowable("Error occurred while creating a component"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnError(e -> emitError(session, e, message))
                // extract the configuration fields when config provided in message
                .flatMap(component -> {
                    if (StringUtils.isNotBlank(message.getConfig())) {
                        return coursewareService.saveConfigurationFields(component.getId(), message.getConfig())
                                .then(Mono.just(component));
                    }
                    return Mono.just(component);
                })
                .flatMap(component -> componentService.getComponentPayload(component.getId()));
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE);
        Mono.zip(componentPayloadMono, rootElementIdMono).subscribe(tuple2 -> {
            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_COMPONENT_CREATE_OK,
                                                                                 message.getId());
            basicResponseMessage.addField("component", tuple2.getT1());
            Responses.writeReactive(session, basicResponseMessage);

            rtmEventBroker.broadcast(message.getType(), getData(message, tuple2.getT1(), account));
            componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext,
                                                                           tuple2.getT2(),
                                                                           tuple2.getT1().getComponentId()).produce();
        });
    }

    private CoursewareElementBroadcastMessage getData(CreateInteractiveComponentMessage message, ComponentPayload payload,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.CREATED)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(payload.getComponentId(), CoursewareElementType.COMPONENT))
                .setParentElement(CoursewareElement.from(message.getInteractiveId(), CoursewareElementType.INTERACTIVE));
    }

    private void emitError(Session session, Throwable throwable, CreateInteractiveComponentMessage message) {
        String errorMessage = throwable.getMessage();
        int code = HttpStatus.SC_BAD_REQUEST;

        if (throwable instanceof ComponentAlreadyExistsFault) {
            code = HttpStatus.SC_CONFLICT;
            errorMessage = String.format("Component id %s already exists", message.getComponentId());
        }

        ErrorMessage error = new ErrorMessage(AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR)
                .setCode(code)
                .setReplyTo(message.getId())
                .setMessage(errorMessage);
        Responses.writeReactive(session, error);
    }
}
