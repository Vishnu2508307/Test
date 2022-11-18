package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmDoesNotThrow;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.workspace.DeleteProjectActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

public class DeleteProjectActivityMessageHandler implements MessageHandler<DeleteProjectActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteProjectActivityMessageHandler.class);

    public static final String PROJECT_ACTIVITY_DELETE = "project.activity.delete";
    private static final String PROJECT_ACTIVITY_DELETE_OK = "project.activity.delete.ok";
    private static final String PROJECT_ACTIVITY_DELETE_ERROR = "project.activity.delete.error";

    private final ActivityService activityService;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Inject
    public DeleteProjectActivityMessageHandler(final ActivityService activityService, final AuthenticationContextProvider authenticationContextProvider, final Provider<RTMEventBroker> rtmEventBrokerProvider) {
        this.activityService = activityService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(final DeleteProjectActivityMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activityId is required");
        affirmArgument(message.getProjectId() != null, "projectId is required");
        affirmDoesNotThrow(() -> activityService.findById(message.getActivityId()).block(), new NotFoundFault("Activity not found"));
        DeletedActivity deletedActivity = activityService.fetchDeletedActivityById(message.getActivityId()).block();
        if (deletedActivity != null) {
            throw new NotFoundFault("Activity not found");
        }
    }

    @Override
    public void handle(final Session session, final DeleteProjectActivityMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();

        // todo save account id for delete?
        activityService.deleteFromProject(message.getActivityId(), message.getProjectId(), account.getId())
                .doOnEach(log.reactiveErrorThrowable("error deleting  activity", throwable -> new HashMap<String, Object>() {
                    {
                        put("activityId", message.getActivityId());
                        put("projectId", message.getProjectId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(aVoid -> {
                    // nothing to do here
                }, ex -> {
                    log.jsonDebug("error deleting the activity", new HashMap<String, Object>() {
                        {
                            put("activityId", message.getActivityId());
                            put("projectId", message.getProjectId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_DELETE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting the root activity");

                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(PROJECT_ACTIVITY_DELETE_OK, message.getId()));
                    rtmEventBroker.broadcast(message.getType(), getData(message, account));
                });
    }

    private CoursewareElementBroadcastMessage getData(DeleteProjectActivityMessage message, Account account) {

        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.DELETED)
                .setElement(CoursewareElement.from(message.getActivityId(), CoursewareElementType.ACTIVITY))
                .setAccountId(account.getId())
                .setProjectId(message.getProjectId());
    }
}
