package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

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
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ComponentAlreadyExistsFault;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.CreateActivityComponentMessage;
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

public class CreateActivityComponentMessageHandler implements MessageHandler<CreateActivityComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateActivityComponentMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_COMPONENT_CREATE = "author.activity.component.create";
    private static final String AUTHOR_ACTIVITY_COMPONENT_CREATE_OK = "author.activity.component.create.ok";
    static final String AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR = "author.activity.component.create.error";

    private final ActivityService activityService;
    private final PluginService pluginService;
    private final ComponentService componentService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentCreatedRTMProducer componentCreatedRTMProducer;

    @Inject
    public CreateActivityComponentMessageHandler(ActivityService activityService,
                                                 PluginService pluginService,
                                                 ComponentService componentService,
                                                 Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                 CoursewareService coursewareService,
                                                 AuthenticationContextProvider authenticationContextProvider,
                                                 Provider<RTMClientContext> rtmClientContextProvider,
                                                 ComponentCreatedRTMProducer componentCreatedRTMProducer) {
        this.activityService = activityService;
        this.pluginService = pluginService;
        this.componentService = componentService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentCreatedRTMProducer = componentCreatedRTMProducer;
    }

    @Override
    public void validate(CreateActivityComponentMessage message) throws RTMValidationException {
        try {

            checkArgument(message.getActivityId() != null, "activityId is required");
            checkArgument(message.getPluginId() != null, "pluginId is required");
            checkArgument(StringUtils.isNotBlank(message.getPluginVersionExpr()), "pluginVersion is required");

            activityService.findById(message.getActivityId())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching activity"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

            pluginService.findLatestVersion(message.getPluginId(), message.getPluginVersionExpr())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching latest plugin version"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR);
        } catch (ActivityNotFoundException e) {
            throw new RTMValidationException("invalid activity", message.getId(), AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR);
        } catch (VersionParserFault e) {
            throw new RTMValidationException("invalid pluginVersion", message.getId(), AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR);
        } catch (PluginNotFoundFault e) {
            throw new RTMValidationException("invalid plugin", message.getId(), AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR);
        }
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, CreateActivityComponentMessage message) {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Mono<Component> componentMono;

        if (message.getComponentId() != null) {
            componentMono = componentService.createForActivity(
                    message.getActivityId(),
                    message.getPluginId(),
                    message.getPluginVersionExpr(),
                    message.getConfig(),
                    message.getComponentId());
        } else {
            componentMono = componentService.createForActivity(
                    message.getActivityId(),
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
                                .then(Mono.just(component))
                                .doOnEach(ReactiveTransaction.linkOnNext());
                    }
                    return Mono.just(component);
                })
                .flatMap(component -> componentService.getComponentPayload(
                        component.getId()).doOnEach(
                        ReactiveTransaction.linkOnNext()));
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getActivityId(), ACTIVITY);
        Mono.zip(componentPayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                            AUTHOR_ACTIVITY_COMPONENT_CREATE_OK,
                            message.getId());
                    basicResponseMessage.addField("component", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    rtmEventBroker.broadcast(message.getType(), getData(message, tuple2.getT1(), account));
                    componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext,
                                                                                   tuple2.getT2(),
                                                                                   tuple2.getT1().getComponentId()).produce();
                });
    }

    private CoursewareElementBroadcastMessage getData(CreateActivityComponentMessage message, ComponentPayload payload,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setParentElement(CoursewareElement.from(message.getActivityId(), ACTIVITY))
                .setElement(CoursewareElement.from(payload.getComponentId(), CoursewareElementType.COMPONENT))
                .setAccountId(account.getId())
                .setAction(CoursewareAction.CREATED);
    }

    private void emitError(Session session, Throwable throwable, CreateActivityComponentMessage message) {
        String errorMessage = throwable.getMessage();
        int code = HttpStatus.SC_BAD_REQUEST;

        if (throwable instanceof ComponentAlreadyExistsFault) {
            code = HttpStatus.SC_CONFLICT;
            errorMessage = String.format("Component id %s already exists", message.getComponentId());
        }

        ErrorMessage error = new ErrorMessage(AUTHOR_ACTIVITY_COMPONENT_CREATE_ERROR)
                .setCode(code)
                .setReplyTo(message.getId())
                .setMessage(errorMessage);
        Responses.writeReactive(session, error);
    }
}
