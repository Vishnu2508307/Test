package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.activity.DuplicateActivityProjectMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import reactor.core.publisher.Mono;

public class DuplicateActivityProjectMessageHandler implements MessageHandler<DuplicateActivityProjectMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DuplicateActivityProjectMessageHandler.class);

    public static final String PROJECT_ACTIVITY_DUPLICATE = "project.activity.duplicate";
    private static final String PROJECT_ACTIVITY_DUPLICATE_OK = "project.activity.duplicate.ok";
    private static final String PROJECT_ACTIVITY_DUPLICATE_ERROR = "project.activity.duplicate.error";

    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public DuplicateActivityProjectMessageHandler(final ActivityService activityService,
                                                  final CoursewareService coursewareService,
                                                  final Provider<AuthenticationContext> authenticationContextProvider) {
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(DuplicateActivityProjectMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activityId is required");
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(Session session, DuplicateActivityProjectMessage message) throws WriteResponseException {
        // get feature flag value
        Boolean newDuplicateFlow = message.getNewDuplicateFlow();

        Account account = authenticationContextProvider.get().getAccount();

        Mono<ActivityPayload> payload = activityService.isDuplicatedCourseInTheSameProject(message.getActivityId(), message.getProjectId(), newDuplicateFlow)
                .flatMap(isInSameProject -> coursewareService.duplicateActivity(message.getActivityId(), account, isInSameProject))
                .flatMap(activity -> {
                    return activityService.addToProject(activity.getId(), message.getProjectId())
                            .thenReturn(activity);
                })
                .flatMap(activityService::getActivityPayload)
                .doOnEach(log.reactiveErrorThrowable("error duplicating project activity"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT));

        payload.single()
                .subscribe(activityPayload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(PROJECT_ACTIVITY_DUPLICATE_OK,
                            message.getId());
                    basicResponseMessage.addField("activity", activityPayload);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    if (ex instanceof ActivityNotFoundException) {
                        Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_DUPLICATE_ERROR,
                                HttpStatus.SC_NOT_FOUND, "Activity not found");
                    } else {
                        Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_DUPLICATE_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to duplicate activity");
                    }
                });
    }
}
