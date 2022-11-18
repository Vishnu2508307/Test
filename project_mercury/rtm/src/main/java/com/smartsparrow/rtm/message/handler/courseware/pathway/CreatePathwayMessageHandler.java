package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayAlreadyExistsFault;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.CreatePathwayMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.created.PathwayCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class CreatePathwayMessageHandler implements MessageHandler<CreatePathwayMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(CreatePathwayMessageHandler.class);

    public static final String AUTHOR_PATHWAY_CREATE = "author.pathway.create";
    private static final String AUTHOR_PATHWAY_CREATE_OK = "author.pathway.create.ok";
    static final String AUTHOR_PATHWAY_CREATE_ERROR = "author.pathway.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final PathwayService pathwayService;
    private final ActivityService activityService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PathwayCreatedRTMProducer pathwayCreatedRTMProducer;

    @Inject
    public CreatePathwayMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                       PathwayService pathwayService,
                                       ActivityService activityService,
                                       CoursewareService coursewareService,
                                       Provider<RTMEventBroker> rtmEventBrokerProvider,
                                       Provider<RTMClientContext> rtmClientContextProvider,
                                       PathwayCreatedRTMProducer pathwayCreatedRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.pathwayService = pathwayService;
        this.activityService = activityService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pathwayCreatedRTMProducer = pathwayCreatedRTMProducer;
    }

    @Override
    public void validate(CreatePathwayMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getActivityId() != null, "missing activityId");
            checkArgument(message.getPathwayType() != null, "missing pathwayType");

            activityService.findById(message.getActivityId()).block();

        } catch (IllegalArgumentException e) {
            logger.jsonDebug("Pathway Mandatory Fields are Missing",   new HashMap<String,Object>() {
                {
                    put("messageId",message.getId());
                    put("message",message.toString());
                    put("errorCode",AUTHOR_PATHWAY_CREATE_ERROR);
                }
            } );
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_PATHWAY_CREATE_ERROR);
        } catch (ActivityNotFoundException e) {
            logger.jsonDebug("Unable to find Activity With Id",   new HashMap<String,Object>() {
                {
                    put("messageId",message.getId());
                    put("message",message.toString());
                    put("errorCode",AUTHOR_PATHWAY_CREATE_ERROR);
                }
            } );
            throw new RTMValidationException("invalid activityId", message.getId(), AUTHOR_PATHWAY_CREATE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PATHWAY_CREATE)
    @Override
    public void handle(Session session, CreatePathwayMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();

        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<Pathway> pathwayMono;
        if (message.getPathwayId() != null) {
            pathwayMono = pathwayService.create(account.getId(),
                                                message.getActivityId(),
                                                message.getPathwayType(),
                                                message.getPathwayId(),
                                                message.getPreloadPathway());
        } else {
            pathwayMono = pathwayService.create(account.getId(),
                                                message.getActivityId(),
                                                message.getPathwayType(),
                                                message.getPreloadPathway());
        }

        Mono<PathwayPayload> pathwayPayloadMono = pathwayMono.flatMap(pathway -> {

                    Mono<String> saveConfigMono = Mono.just("");

                    if (StringUtils.isNotBlank(message.getConfig())) {
                        saveConfigMono = pathwayService.replaceConfig(pathway.getId(), message.getConfig())
                                .thenReturn(message.getConfig());
                    }

                    return saveConfigMono.then(pathwayService.getPathwayPayload(pathway));
                })
                .doOnEach(logger.reactiveErrorThrowable("Error occured while creating Pathway",
                                                        throwable -> new HashMap<String, Object>() {
                                                            {
                                                                put("messageId", message.getId());
                                                                put("activityId", message.getActivityId());
                                                                put("pathwayType", message.getPathwayType());
                                                                put("config", message.getConfig());
                                                            }
                                                        }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getActivityId(), ACTIVITY);
        Mono.zip(pathwayPayloadMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_PATHWAY_CREATE_OK,
                                                                                         message.getId());
                    basicResponseMessage.addField("pathway", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    rtmEventBroker.broadcast(message.getType(), getData(message, tuple2.getT1(), account));
                    pathwayCreatedRTMProducer.buildPathwayCreatedRTMConsumable(rtmClientContext,
                                                                               tuple2.getT2(),
                                                                               tuple2.getT1().getPathwayId()).produce();

                }, ex -> {
                    logger.jsonDebug("Unable to create pathway with type for activity", new HashMap<String, Object>() {
                        {
                            put("pathWayType", message.getPathwayType());
                            put("activityId", message.getActivityId());
                            put("Exception", ex);
                        }
                    });

                    String errorMessage = "Unable to create pathway";
                    int code = HttpStatus.SC_UNPROCESSABLE_ENTITY;

                    if (ex instanceof PathwayAlreadyExistsFault) {
                        code = HttpStatus.SC_CONFLICT;
                        errorMessage = String.format("Pathway id %s already exists", message.getPathwayId());
                    }

                    Responses.errorReactive(session, message.getId(), AUTHOR_PATHWAY_CREATE_ERROR, code, errorMessage);
                });
    }

    private CoursewareElementBroadcastMessage getData(CreatePathwayMessage message, PathwayPayload pathwayPayload,
                                                      Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAccountId(account.getId())
                .setParentElement(CoursewareElement.from(message.getActivityId(), ACTIVITY))
                .setElement(CoursewareElement.from(pathwayPayload.getPathwayId(), CoursewareElementType.PATHWAY))
                .setAction(CoursewareAction.CREATED);
    }
}
