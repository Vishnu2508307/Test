package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
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
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.MoveComponentsMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.ComponentMovedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class MoveComponentsMessageHandler implements MessageHandler<MoveComponentsMessage> {

    public static final String AUTHOR_COMPONENTS_MOVE = "author.components.move";
    static final String AUTHOR_COMPONENTS_MOVE_OK = "author.components.move.ok";
    static final String AUTHOR_COMPONENTS_MOVE_ERROR = "author.components.move.error";
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MoveComponentsMessageHandler.class);
    private final ComponentService componentService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentMovedRTMProducer componentMovedRTMProducer;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public MoveComponentsMessageHandler(ComponentService componentService,
                                        final CoursewareService coursewareService,
                                        final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                        final Provider<RTMClientContext> rtmClientContextProvider,
                                        final ComponentMovedRTMProducer componentMovedRTMProducer,
                                        final Provider<AuthenticationContext> authenticationContextProvider) {
        this.componentService = componentService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentMovedRTMProducer = componentMovedRTMProducer;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "no null pointer since affirmArgument ensures parent is not null before checks on fields")
    @Override
    public void validate(MoveComponentsMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getComponentIds(), "componentIds are required");
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");

        List<ParentByComponent> parentComponents = componentService.findParentForComponents(message.getComponentIds())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching parent for component"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .block();

        parentComponents.stream().forEach(parentByComponent -> affirmArgument(parentByComponent != null,
                String.format("parent not found for component %s", parentByComponent.getComponentId())));
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, MoveComponentsMessage message) {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        componentService.move(message.getComponentIds(), message.getElementId(), message.getElementType())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while moving a component"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getElementId(), message.getElementType()))
                .subscribe(rootElementId -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_COMPONENTS_MOVE_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);

                               // produce consumable events for moved components
                               buildComponentsMovedConsumable(rootElementId, message, rtmEventBroker, account,
                                                                rtmClientContext);
                           },
                           ex -> {
                                log.jsonError(
                                        "Unable to move components{}",
                                        new HashMap<String, Object>() {
                                            {
                                                put("message", message.toString());
                                                put("error", ex.getStackTrace());
                                            }
                                        },
                                        ex);
                                Responses.errorReactive(session, message.getId(), AUTHOR_COMPONENTS_MOVE_ERROR,
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to move components");
                           }
                );
    }

    private void buildComponentsMovedConsumable(final UUID rootElementId,
                                                  final MoveComponentsMessage message,
                                                  final RTMEventBroker rtmEventBroker,
                                                  final Account account,
                                                  final RTMClientContext rtmClientContext) {
        // produce events for each moved component
        message.getComponentIds()
                .stream()
                .forEach(component -> {
                    rtmEventBroker.broadcast(message.getType(), getData(message, component, account));

                    componentMovedRTMProducer.buildComponentMovedRTMConsumable(rtmClientContext,
                                                                               rootElementId,
                                                                               component).produce();
                });
    }

    private CoursewareElementBroadcastMessage getData(final MoveComponentsMessage message,
                                                      final UUID componentId,
                                                      final Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.COMPONENT_MOVED)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(componentId, COMPONENT))
                .setParentElement(CoursewareElement.from(message.getElementId(), message.getElementType()));
    }

}
