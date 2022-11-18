package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

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
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.DeleteInteractiveComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Deprecated
public class DeleteInteractiveComponentMessageHandler implements MessageHandler<DeleteInteractiveComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteInteractiveComponentMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_COMPONENT_DELETE = "author.interactive.component.delete";
    private static final String AUTHOR_INTERACTIVE_COMPONENT_DELETE_OK = "author.interactive.component.delete.ok";
    static final String AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR = "author.interactive.component.delete.error";

    private final ComponentService componentService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentDeletedRTMProducer componentDeletedRTMProducer;

    @Inject
    public DeleteInteractiveComponentMessageHandler(ComponentService componentService,
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
    public void validate(DeleteInteractiveComponentMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getComponentId() != null, "componentId is required");
            checkArgument(message.getInteractiveId() != null, "interactiveId is required");

            ParentByComponent parent = componentService.findParentFor(message.getComponentId())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching parent for component"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

            checkArgument(parent != null, String.format("parent interactive not found for component %s",
                    message.getComponentId()));
            checkArgument(CoursewareElementType.INTERACTIVE.equals(parent.getParentType()),
                    "parent component is not an INTERACTIVE");
            checkArgument(message.getInteractiveId().equals(parent.getParentId()),
                    String.format("found interactive not matching interactiveId %s", message.getInteractiveId()));

        } catch (IllegalArgumentException | ComponentParentNotFound e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_COMPONENT_DELETE)
    @Override
    public void handle(Session session, DeleteInteractiveComponentMessage message) {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        componentService.deleteInteractiveComponent(message.getComponentId(), message.getInteractiveId())
                .doOnEach(log.reactiveErrorThrowable("Error while deleting interactive component"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE))
                .subscribe(rootElementId -> {
                               componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext,
                                                                                              rootElementId,
                                                                                              message.getComponentId()).produce();
                           },
                           ex -> emitError(session, message, ex),
                           () -> {
                               emitSuccess(session, message);

                               rtmEventBroker.broadcast(message.getType(),
                                                        getData(message, account));
                           });
    }

    private CoursewareElementBroadcastMessage getData(DeleteInteractiveComponentMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.DELETED)
                .setAccountId(account.getId())
                .setElement(CoursewareElement.from(message.getComponentId(), COMPONENT))
                .setParentElement(CoursewareElement.from(message.getInteractiveId(), CoursewareElementType.INTERACTIVE));
    }

    public void emitSuccess(Session session, DeleteInteractiveComponentMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_INTERACTIVE_COMPONENT_DELETE_OK, message.getId())
                .addField("componentId", message.getComponentId())
                .addField("interactiveId", message.getInteractiveId()));
    }

    public void emitError(Session session, DeleteInteractiveComponentMessage message, Throwable ex) {
        String errorMessage = String.format("error deleting %s", message.toString());
        Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_COMPONENT_DELETE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, errorMessage);
    }
}
