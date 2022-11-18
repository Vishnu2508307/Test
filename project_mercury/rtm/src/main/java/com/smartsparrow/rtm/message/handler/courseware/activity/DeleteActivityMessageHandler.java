package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.DeleteActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.ActivityDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteActivityMessageHandler implements MessageHandler<DeleteActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteActivityMessageHandler.class);

    public static final String AUTHOR_ACTIVITY_DELETE = "author.activity.delete";
    private static final String AUTHOR_ACTIVITY_DELETE_OK = "author.activity.delete.ok";
    private static final String AUTHOR_ACTIVITY_DELETE_ERROR = "author.activity.delete.error";

    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityDeletedRTMProducer activityDeletedRTMProducer;

    @Inject
    public DeleteActivityMessageHandler(ActivityService activityService,
                                        CoursewareService coursewareService,
                                        Provider<RTMEventBroker> rtmEventBrokerProvider,
                                        AuthenticationContextProvider authenticationContextProvider,
                                        Provider<RTMClientContext> rtmClientContextProvider,
                                        ActivityDeletedRTMProducer activityDeletedRTMProducer) {
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityDeletedRTMProducer = activityDeletedRTMProducer;
    }

    /**
     * Validate that all the message field are supplied and valid. It also validates that the supplied parentPathwayId
     * is in fact the valid parent of the supplied activity id
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the validation fails
     */
    @Override
    public void validate(DeleteActivityMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getActivityId() != null, "activityId is required");
            checkArgument(message.getParentPathwayId() != null, "parentPathwayId is required");
            checkArgument(activityService.findById(message.getActivityId()).block() != null,
                    String.format("activity %s not found", message.getActivityId()));

            UUID parentPathwayId = activityService.findParentPathwayId(message.getActivityId())
                    .block();

            checkArgument(parentPathwayId != null, "parentPathwayId not found for activity %s",
                    message.getActivityId());
            checkArgument(message.getParentPathwayId().equals(parentPathwayId),
                    "supplied parentPathwayId does not match the activity parent");
        } catch (IllegalArgumentException | ActivityNotFoundException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ACTIVITY_DELETE_ERROR);
        }
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, DeleteActivityMessage message) throws WriteResponseException {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        activityService.delete(message.getActivityId(), message.getParentPathwayId(), account.getId())
                .doOnEach(log.reactiveErrorThrowable("error deleting activity",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getActivityId());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getParentPathwayId(), PATHWAY))
                .subscribe(rootElementId -> {
                               activityDeletedRTMProducer.buildActivityDeletedRTMConsumable(rtmClientContext,
                                                                                            rootElementId,
                                                                                            message.getActivityId(),
                                                                                            message.getParentPathwayId()).produce();
                           },
                           ex -> emitError(session, message, ex),
                           () -> {
                               emitSuccess(session, message);
                               rtmEventBroker.broadcast(message.getType(), getData(message, account));
                           });
    }

    private CoursewareElementBroadcastMessage getData(DeleteActivityMessage message, Account account) {

        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.DELETED)
                .setElement(CoursewareElement.from(message.getActivityId(), CoursewareElementType.ACTIVITY))
                .setParentElement(CoursewareElement.from(message.getParentPathwayId(), PATHWAY))
                .setAccountId(account.getId());
    }

    private void emitSuccess(Session session, DeleteActivityMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ACTIVITY_DELETE_OK, message.getId())
                .addField("activityId", message.getActivityId())
                .addField("parentPathwayId", message.getParentPathwayId()));
    }

    private void emitError(Session session, DeleteActivityMessage message, Throwable ex) {
        log.jsonDebug("unable to delete activity ", new HashMap<String, Object>() {
            {
                put("activityId", message.getActivityId());
                put("error", ex.getStackTrace());
            }
        });
        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_DELETE_ERROR,
                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting activity");
    }
}
