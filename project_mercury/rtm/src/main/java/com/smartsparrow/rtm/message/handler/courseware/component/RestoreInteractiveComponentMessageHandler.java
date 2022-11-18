package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.RestoreInteractiveComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class RestoreInteractiveComponentMessageHandler implements MessageHandler<RestoreInteractiveComponentMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RestoreInteractiveComponentMessageHandler.class);

    public final static String AUTHOR_INTERACTIVE_COMPONENT_RESTORE = "author.interactive.component.restore";
    public final static String AUTHOR_INTERACTIVE_COMPONENT_RESTORE_OK = "author.interactive.component.restore.ok";
    public final static String AUTHOR_INTERACTIVE_COMPONENT_RESTORE_ERROR = "author.interactive.component.restore.error";

    private final ComponentService componentService;
    private final InteractiveService interactiveService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentCreatedRTMProducer componentCreatedRTMProducer;

    @Inject
    public RestoreInteractiveComponentMessageHandler(ComponentService componentService,
                                                     InteractiveService interactiveService,
                                                     Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                     CoursewareService coursewareService,
                                                     AuthenticationContextProvider authenticationContextProvider,
                                                     Provider<RTMClientContext> rtmClientContextProvider,
                                                     ComponentCreatedRTMProducer componentCreatedRTMProducer) {
        this.componentService = componentService;
        this.interactiveService = interactiveService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentCreatedRTMProducer = componentCreatedRTMProducer;
    }

    @Override
    public void validate(RestoreInteractiveComponentMessage message) {
        affirmArgument(message.getInteractiveId() != null, "interactiveId is required");
        affirmArgumentNotNullOrEmpty(message.getComponentIds(), "componentId is required");

        Interactive interactive = interactiveService.findById(message.getInteractiveId())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching interactive"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .block();

        affirmArgument(interactive != null, "interactive not found");

    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_COMPONENT_RESTORE)
    @Override
    public void handle(Session session, RestoreInteractiveComponentMessage message) {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<List<Component>> componentMono = componentService.restoreComponent(message.getComponentIds(),
                                                                                message.getInteractiveId(),
                                                                                INTERACTIVE)
                .doOnEach(log.reactiveErrorThrowable("Error while restoring component",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("componentIdsx", message.getComponentIds());
                                                             put("interactiveId", message.getInteractiveId());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList();

        Mono<UUID> rootElementId = coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE);
        Mono.zip(componentMono, rootElementId)
                .subscribe(tuple2 -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_INTERACTIVE_COMPONENT_RESTORE_OK,
                                       message.getId());
                               basicResponseMessage.addField("components", tuple2.getT1());
                               Responses.writeReactive(session, basicResponseMessage);
                               // produce consumable events for components
                               buildComponentRestoredConsumable(tuple2.getT1(), tuple2.getT2(), message, rtmEventBroker, account,
                                                                rtmClientContext);
                           },
                           ex -> {
                               log.jsonError(
                                       "Unable to restore component for an interactive{}",
                                       new HashMap<String, Object>() {
                                           {
                                               put("message",
                                                   message.toString());
                                               put("error", ex.getStackTrace());
                                           }
                                       },
                                       ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_INTERACTIVE_COMPONENT_RESTORE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to restore component for an interactive");
                           }
                );
    }

    private void buildComponentRestoredConsumable(final List<Component> components,
                                                  final UUID rootElementId,
                                                  final RestoreInteractiveComponentMessage message,
                                                  final RTMEventBroker rtmEventBroker,
                                                  final Account account,
                                                  final RTMClientContext rtmClientContext) {
        // produce events for each recreated components
        components.stream().forEach(component -> {
            rtmEventBroker.broadcast(message.getType(),
                                     getData(message, component.getId(), account));

            componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext,
                                                                           rootElementId,
                                                                           component.getId()).produce();
        });
    }

    private CoursewareElementBroadcastMessage getData(RestoreInteractiveComponentMessage message,
                                                      UUID componentId,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.CREATED)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(componentId, CoursewareElementType.COMPONENT))
                .setParentElement(CoursewareElement.from(message.getInteractiveId(),
                                                         CoursewareElementType.INTERACTIVE));
    }
}
