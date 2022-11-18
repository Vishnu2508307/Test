package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
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
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.DeleteInteractiveComponentsMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DeleteInteractiveComponentsMessageHandler implements MessageHandler<DeleteInteractiveComponentsMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(
            DeleteInteractiveComponentsMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_COMPONENTS_DELETE = "author.interactive.components.delete";
    public static final String AUTHOR_INTERACTIVE_COMPONENTS_DELETE_OK = "author.interactive.components.delete.ok";
    private static final String AUTHOR_INTERACTIVE_COMPONENTS_DELETE_ERROR = "author.interactive.components.delete.error";

    private final ComponentService componentService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentDeletedRTMProducer componentDeletedRTMProducer;

    @Inject
    public DeleteInteractiveComponentsMessageHandler(ComponentService componentService,
                                                     Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                     AuthenticationContextProvider authenticationContextProvider,
                                                     CoursewareService coursewareService,
                                                     Provider<RTMClientContext> rtmClientContextProvider,
                                                     ComponentDeletedRTMProducer componentDeletedRTMProducer) {
        this.componentService = componentService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentDeletedRTMProducer = componentDeletedRTMProducer;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "no null pointer since checkArgument ensures parent is not null before checks on fields")
    @Override
    public void validate(DeleteInteractiveComponentsMessage message) {
        affirmArgumentNotNullOrEmpty(message.getComponentIds(), "componentId is required");
        affirmArgument(message.getInteractiveId() != null, "interactiveId is required");

        List<ParentByComponent> parentComponents = componentService.findParentForComponents(message.getComponentIds())
                .doOnEach(log.reactiveErrorThrowable("Error while fetching parent for component"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .block();

        parentComponents.stream().forEach(parentByComponent -> {
            affirmArgument(parentByComponent != null,
                           String.format("parent interactive not found for component %s",
                                         parentByComponent.getComponentId()));
            affirmArgument(CoursewareElementType.INTERACTIVE.equals(parentByComponent.getParentType()),
                           "parent component is not an INTERACTIVE");
            affirmArgument(message.getInteractiveId().equals(parentByComponent.getParentId()),
                           String.format("found interactive not matching interactiveId %s",
                                         message.getInteractiveId()));
        });

    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_COMPONENTS_DELETE)
    @Override
    public void handle(Session session, DeleteInteractiveComponentsMessage message) {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        componentService.deleteInteractiveComponents(message.getComponentIds(), message.getInteractiveId())
                .doOnEach(log.reactiveErrorThrowable("Error while deleting interactive components"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE))
                .subscribe(rootElementId -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_INTERACTIVE_COMPONENTS_DELETE_OK,
                                       message.getId())
                                       .addField("componentIds", message.getComponentIds())
                                       .addField("interactiveId", message.getInteractiveId());
                               Responses.writeReactive(session, basicResponseMessage);

                               // produce consumable events for delete components
                               buildComponentsDeletedConsumable(rootElementId, message, rtmEventBroker, account,
                                                                rtmClientContext);
                           },
                           ex -> {
                               log.jsonError(
                                       "Unable to delete components for an interactive{}",
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
                                                       AUTHOR_INTERACTIVE_COMPONENTS_DELETE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to delete components for an interactive");
                           }
                );
    }

    private void buildComponentsDeletedConsumable(final UUID rootElementId,
                                                  final DeleteInteractiveComponentsMessage message,
                                                  final RTMEventBroker rtmEventBroker,
                                                  final Account account,
                                                  final RTMClientContext rtmClientContext) {
        // produce events for each deleted components
        message.getComponentIds()
                .stream()
                .forEach(component -> {
                    rtmEventBroker.broadcast(message.getType(), getData(message, component, account));

                    componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext,
                                                                                   rootElementId,
                                                                                   component).produce();
                });
    }

    private CoursewareElementBroadcastMessage getData(final DeleteInteractiveComponentsMessage message,
                                                      final UUID componentId,
                                                      final Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.DELETED)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(componentId, COMPONENT))
                .setParentElement(CoursewareElement.from(message.getInteractiveId(),
                                                         CoursewareElementType.INTERACTIVE));
    }

}
