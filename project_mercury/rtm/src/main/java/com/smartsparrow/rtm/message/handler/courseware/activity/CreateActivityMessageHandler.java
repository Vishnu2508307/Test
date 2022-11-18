package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityAlreadyExistsFault;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.CreateActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ActivityCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class CreateActivityMessageHandler implements MessageHandler<CreateActivityMessage> {

    public static final String AUTHOR_ACTIVITY_CREATE = "author.activity.create";
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateActivityMessageHandler.class);
    private static final String AUTHOR_ACTIVITY_CREATE_OK = "author.activity.create.ok";
    private static final String AUTHOR_ACTIVITY_CREATE_ERROR = "author.activity.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityCreatedRTMProducer activityCreatedRTMProducer;

    @Inject
    public CreateActivityMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                        ActivityService activityService,
                                        PathwayService pathwayService,
                                        Provider<RTMEventBroker> rtmEventBrokerProvider,
                                        CoursewareService coursewareService,
                                        Provider<RTMClientContext> rtmClientContextProvider,
                                        ActivityCreatedRTMProducer activityCreatedRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityCreatedRTMProducer = activityCreatedRTMProducer;
    }

    @Override
    public void validate(CreateActivityMessage message){
        affirmArgument(message.getPluginId() != null, "missing plugin id parameter");
        affirmArgument(!Strings.isNullOrEmpty(message.getPluginVersionExpr()), "plugin version expression required");
        affirmArgument(message.getParentPathwayId() != null, "parentPathwayId is required");
        // check that the supplied pathway id is valid
        affirmArgument(pathwayService.findById(message.getParentPathwayId()).block() != null,
                      String.format("parentPathwayId `%s` not found", message.getParentPathwayId()));
    }

    @Trace(dispatcher = true)
    @SuppressWarnings("Duplicates")
    @Override
    public void handle(Session session, CreateActivityMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        Mono<Activity> createActivityMono = getCreateActivityFor(message, account.getId());

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<ActivityPayload> activityPayloadMono = createActivityMono
                .flatMap(activity -> {

                    Mono<String> saveConfigMono = Mono.just("");
                    Mono<ActivityTheme> saveThemeConfigMono = Mono.just(new ActivityTheme());

                    //If there is an activity config use the replace message
                    if (StringUtils.isNotBlank(message.getConfig())) {
                        saveConfigMono = activityService.replaceConfig(account.getId(),
                                                                       activity.getId(),
                                                                       message.getConfig())
                                .thenMany(coursewareService.saveConfigurationFields(activity.getId(),
                                                                                    message.getConfig()))
                                .then(Mono.just(message.getConfig()));
                    }

                    //If there is an activity theme then use the replace activity theme message
                    if (StringUtils.isNotBlank(message.getTheme())) {
                        saveThemeConfigMono = activityService.replaceActivityThemeConfig(activity.getId(),
                                                                                         message.getTheme());
                    }

                    return saveConfigMono
                            .then(saveThemeConfigMono)
                            .thenReturn(activity);
                })
                .flatMap(activityService::getActivityPayload)
                .doOnEach(log.reactiveErrorThrowable("error creating the activity",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", message.getElementId());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getParentPathwayId(), PATHWAY);
        Mono.zip(activityPayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                               emitResponse(session, message, tuple2.getT1());
                               rtmEventBroker.broadcast(message.getType(), getData(message, tuple2.getT1(), account));
                               activityCreatedRTMProducer.buildActivityCreatedRTMConsumable(rtmClientContext,
                                                                                            tuple2.getT2(),
                                                                                            tuple2.getT1().getActivityId(),
                                                                                            message.getParentPathwayId())
                                       .produce();
                           },
                           ex -> emitError(session, message, ex)
                );
    }

    /**
     * Decide at the handler level if the activity should be created as a top level activity or associated to an
     * exiting parent pathway id.
     *
     * @param message   the incoming message
     * @param creatorId the account trying to create the activity
     * @return a created top level activity mono when the parentPathwayId optional message parameter is not supplied
     * or a created activity mono that has been associated to a parentPathwayId when this has been supplied.
     */
    @Trace(async = true)
    private Mono<Activity> getCreateActivityFor(CreateActivityMessage message, UUID creatorId) {
        return activityService.create(creatorId, message.getPluginId(), message.getParentPathwayId(),
                                      message.getPluginVersionExpr(), message.getActivityId())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    private void emitResponse(Session session, CreateActivityMessage message, ActivityPayload activity) {
        // limit the fields returned in this message.
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ACTIVITY_CREATE_OK,
                message.getId());
        basicResponseMessage.addField("activity", activity);
        Responses.writeReactive(session, basicResponseMessage);
    }

    @SuppressWarnings("Duplicates")
    private void emitError(Session session, CreateActivityMessage message, Throwable ex) {
        log.jsonDebug("Activity can't be created ", new HashMap<String, Object>() {
            {
                put("activityId", message.getElementId());
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
        Responses.errorReactive(session, message.getId(), AUTHOR_ACTIVITY_CREATE_ERROR, code, errorMessage);
    }

    /**
     * Build the data that the {@link RTMEventBroker} will send with the event message. If the parent pathway is
     * <code>null</code> then the parent courseware element is set to <code>null</code>.
     *
     * @param message  the received message
     * @param activity the activity payload
     * @return the data
     */
    private CoursewareElementBroadcastMessage getData(CreateActivityMessage message, ActivityPayload activity, Account account) {

        CoursewareElement parent = CoursewareElement.from(message.getParentPathwayId(), PATHWAY);
        return new CoursewareElementBroadcastMessage()
                .setParentElement(parent)
                .setElement(CoursewareElement.from(activity.getActivityId(), CoursewareElementType.ACTIVITY))
                .setAction(CoursewareAction.CREATED)
                .setAccountId(account.getId());
    }
}
