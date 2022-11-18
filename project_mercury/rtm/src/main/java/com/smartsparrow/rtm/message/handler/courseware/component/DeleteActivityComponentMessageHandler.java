package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
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
import com.smartsparrow.rtm.message.recv.courseware.component.DeleteActivityComponentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ComponentDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DeleteActivityComponentMessageHandler implements MessageHandler<DeleteActivityComponentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteActivityComponentMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_COMPONENT_DELETE = "author.activity.component.delete";
    private static final String AUTHOR_ACTIVITY_COMPONENT_DELETE_OK = "author.activity.component.delete.ok";
    private static final String AUTHOR_ACTIVITY_COMPONENT_DELETE_ERROR = "author.activity.component.delete.error";

    private final ComponentService componentService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ComponentDeletedRTMProducer componentDeletedRTMProducer;

    @Inject
    public DeleteActivityComponentMessageHandler(ComponentService componentService,
                                                 CoursewareService coursewareService,
                                                 Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                 AuthenticationContextProvider authenticationContextProvider,
                                                 Provider<RTMClientContext> rtmClientContextProvider,
                                                 ComponentDeletedRTMProducer componentDeletedRTMProducer) {
        this.componentService = componentService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.componentDeletedRTMProducer = componentDeletedRTMProducer;
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "no null pointer since checkArgument ensures parent is not null before checks on fields")
    @Override
    public void validate(DeleteActivityComponentMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getComponentId() != null, "componentId is required");
            checkArgument(message.getActivityId() != null, "activityId is required");

            ParentByComponent parent = componentService.findParentFor(message.getComponentId())
                    .doOnEach(log.reactiveErrorThrowable("Error while fetching parent for component"))
                    .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                    .block();

            checkArgument(parent != null, String.format("parent activity not found for component %s",
                    message.getComponentId()));
            checkArgument(ACTIVITY.equals(parent.getParentType()),
                          "parent component is not an ACTIVITY");
            checkArgument(message.getActivityId().equals(parent.getParentId()),
                    String.format("found activity not matching activityId %s", message.getActivityId()));

        } catch (IllegalArgumentException | ComponentParentNotFound e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ACTIVITY_COMPONENT_DELETE_ERROR);
        }
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, DeleteActivityComponentMessage message) {

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        componentService.deleteActivityComponent(message.getComponentId(),
                                                                       message.getActivityId())
                .doOnEach(log.reactiveErrorThrowable("Error while deleting activity component"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getActivityId(), ACTIVITY))
                .subscribe(rootElementId -> {
                               componentDeletedRTMProducer.buildComponentDeletedRTMConsumable(rtmClientContext,
                                                                                              rootElementId,
                                                                                              message.getComponentId()).produce();
                           },
                           ex -> emitError(session, message, ex),
                           () -> {
                               emitSuccess(session, message);

                               rtmEventBroker.broadcast(message.getType(), getData(message, account));
                           });
    }

    private CoursewareElementBroadcastMessage getData(DeleteActivityComponentMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setParentElement(CoursewareElement.from(message.getActivityId(), ACTIVITY))
                .setElement(CoursewareElement.from(message.getComponentId(), COMPONENT))
                .setAccountId(account.getId())
                .setAction(CoursewareAction.DELETED);
    }

    public void emitSuccess(Session session, DeleteActivityComponentMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ACTIVITY_COMPONENT_DELETE_OK, message.getId())
                .addField("componentId", message.getComponentId())
                .addField("activityId", message.getActivityId()));
    }

    public void emitError(Session session, DeleteActivityComponentMessage message, Throwable ex) {
        String errorMessage = String.format("error deleting %s", message.toString());
        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_COMPONENT_DELETE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, errorMessage);
    }
}
