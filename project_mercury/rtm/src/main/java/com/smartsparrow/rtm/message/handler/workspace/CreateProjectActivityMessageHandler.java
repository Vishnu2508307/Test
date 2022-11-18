package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityAlreadyExistsFault;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.workspace.CreateProjectActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import reactor.core.publisher.Mono;

public class CreateProjectActivityMessageHandler implements MessageHandler<CreateProjectActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateProjectActivityMessageHandler.class);

    public static final String PROJECT_ACTIVITY_CREATE = "project.activity.create";
    private static final String PROJECT_ACTIVITY_CREATE_OK = "project.activity.create.ok";
    private static final String PROJECT_ACTIVITY_CREATE_ERROR = "project.activity.create.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Inject
    public CreateProjectActivityMessageHandler(final AuthenticationContextProvider authenticationContextProvider,
                                               final ActivityService activityService,
                                               final CoursewareService coursewareService,
                                               final Provider<RTMEventBroker> rtmEventBrokerProvider) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
    }

    @Override
    public void validate(final CreateProjectActivityMessage message) throws RTMValidationException {
        affirmArgument(message.getPluginId() != null, "pluginId is required");
        affirmArgument(!Strings.isNullOrEmpty(message.getPluginVersionExpr()), "pluginVersionExpr is required");
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(final Session session, final CreateProjectActivityMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();

        activityService
                .create(account.getId(), message.getPluginId(), message.getPluginVersionExpr(), message.getActivityId())
                .doOnEach(log.reactiveErrorThrowableIf("Error creating top level activity", throwable ->
                        !(throwable instanceof VersionParserFault || throwable instanceof PluginNotFoundFault)))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .flatMap(activity -> {

                    Mono<String> saveConfigMono = Mono.just("");
                    Mono<ActivityTheme> saveThemeConfigMono = Mono.just(new ActivityTheme());

                    //If there is an activity config use the replace message
                    if (StringUtils.isNotBlank(message.getConfig())) {
                        saveConfigMono = activityService.replaceConfig(account.getId(), activity.getId(), message.getConfig())
                                .thenMany(coursewareService.saveConfigurationFields(activity.getId(), message.getConfig()))
                                .then(Mono.just(message.getConfig()));
                    }

                    //If there is an activity theme then use the replace activity theme message
                    if (StringUtils.isNotBlank(message.getTheme())) {
                        saveThemeConfigMono = activityService.replaceActivityThemeConfig(activity.getId(), message.getTheme());
                    }

                    return saveConfigMono
                            .then(saveThemeConfigMono)
                            .then(activityService.addToProject(activity.getId(), message.getProjectId()))
                            .doOnEach(log.reactiveErrorThrowable("Unable to add to project"))
                            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                            .thenReturn(activity);
                })
                .flatMap(activity -> activityService.getActivityPayload(activity.getId()))
                .subscribe(payload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(PROJECT_ACTIVITY_CREATE_OK, message.getId());
                    basicResponseMessage.addField("activity", payload);

                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setElement(CoursewareElement.from(payload.getActivityId(), CoursewareElementType.ACTIVITY))
                            .setAccountId(account.getId())
                            .setAction(CoursewareAction.CREATED);
                    rtmEventBroker.broadcast(PROJECT_ACTIVITY_CREATE, broadcastMessage);
                }, ex -> {
                    log.jsonDebug("Activity can't be created or added to project", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    String errorMessage = "Unable to save activity";
                    int code = HttpStatus.SC_BAD_REQUEST;
                    if (ex instanceof VersionParserFault) {
                        errorMessage = String.format("Unable to parse version expression '%s'", message.getPluginVersionExpr());
                    } else if (ex instanceof PluginNotFoundFault) {
                        errorMessage = "Plugin not found";
                        code = HttpStatus.SC_NOT_FOUND;
                    } else if (ex instanceof ActivityAlreadyExistsFault) {
                        code = HttpStatus.SC_CONFLICT;
                        errorMessage = String.format("Activity id %s already exists", message.getActivityId());
                    }

                    Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_CREATE_ERROR, code, errorMessage);
                });
    }
}
