package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveAlreadyExistsFault;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.CreateInteractiveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.created.InteractiveCreatedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class CreateInteractiveMessageHandler implements MessageHandler<CreateInteractiveMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(CreateInteractiveMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_CREATE = "author.interactive.create";
    public static final String AUTHOR_INTERACTIVE_CREATE_OK = "author.interactive.create.ok";
    public static final String AUTHOR_INTERACTIVE_CREATE_ERROR = "author.interactive.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final PluginService pluginService;
    private final InteractiveService interactiveService;
    private final PathwayService pathwayService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final InteractiveCreatedRTMProducer interactiveCreatedRTMProducer;

    @Inject
    public CreateInteractiveMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                           PluginService pluginService,
                                           InteractiveService interactiveService,
                                           PathwayService pathwayService,
                                           Provider<RTMEventBroker> rtmEventBrokerProvider,
                                           CoursewareService coursewareService,
                                           Provider<RTMClientContext> rtmClientContextProvider,
                                           InteractiveCreatedRTMProducer interactiveCreatedRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.pluginService = pluginService;
        this.interactiveService = interactiveService;
        this.pathwayService = pathwayService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.interactiveCreatedRTMProducer = interactiveCreatedRTMProducer;
    }

    @Override
    public void validate(CreateInteractiveMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getPathwayId() != null, "missing pathwayId parameter");
            checkArgument(message.getPluginId() != null, "missing pluginId parameter");
            checkArgument(!Strings.isNullOrEmpty(message.getPluginVersionExpr()),
                    "missing pluginVersion parameter");

            // check the plugin exists!
            pluginService.findLatestVersion(message.getPluginId(), message.getPluginVersionExpr()).block();
            //check the pathway exists
            pathwayService.findById(message.getPathwayId()).block();
        } catch (PluginNotFoundFault | VersionParserFault | IllegalArgumentException e) {
            logger.jsonDebug("Invalid Plugin",   new HashMap<String,Object>() {
                {
                  put("message",message.toString());
                }
            });
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_CREATE_ERROR);
        } catch (PathwayNotFoundException e) {
            logger.jsonDebug("Pathway Not Found",  new HashMap<String,Object>() {
                {
                    put("message",message.toString());
                }
            });
            throw new RTMValidationException("invalid pathway", message.getId(), AUTHOR_INTERACTIVE_CREATE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_CREATE)
    public void handle(Session session, CreateInteractiveMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<Interactive> interactiveMono;

        if (message.getInteractiveId() != null) {
            interactiveMono = interactiveService.create(account.getId(), message.getPathwayId(), message.getPluginId(),
                                                        message.getPluginVersionExpr(), message.getInteractiveId());
        } else {
            interactiveMono = interactiveService.create(account.getId(), message.getPathwayId(), message.getPluginId(),
                                                        message.getPluginVersionExpr());
        }
        Mono<InteractivePayload> interactivePayload = interactiveMono
                .flatMap(interactive ->
                        //store config if provided
                        Mono.just(message)
                                .filter(m -> !Strings.isNullOrEmpty(m.getConfig()))
                                .flatMap(m -> interactiveService.replaceConfig(account.getId(), interactive.getId(), message.getConfig()))
                                .flux()
                                .flatMap(m -> coursewareService.saveConfigurationFields(interactive.getId(), message.getConfig()))
                                .then(Mono.just(interactive)))
                .flatMap(interactiveService::getInteractivePayload)
                .doOnEach(logger.reactiveErrorThrowable("Exception occurred while CreatingInteractiveMessage", throwable -> new HashMap<String,Object>() {
                            {
                              put("message",message.toString());
                            }
                }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootActivityId = coursewareService.getRootElementId(message.getPathwayId(), PATHWAY);
        Mono.zip(interactivePayload, rootActivityId).subscribe(
                tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_CREATE_OK, message.getId());
                    basicResponseMessage.addField("interactive", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage =
                            new CoursewareElementBroadcastMessage()
                                    .setParentElement(new CoursewareElement(message.getPathwayId(), PATHWAY))
                                    .setElement(new CoursewareElement(tuple2.getT1().getInteractiveId(), INTERACTIVE))
                                    .setAccountId(account.getId())
                                    .setAction(CoursewareAction.CREATED);

                    rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_CREATE, broadcastMessage);
                    interactiveCreatedRTMProducer.buildInteractiveCreatedRTMConsumable(rtmClientContext,
                                                                                       tuple2.getT2(),
                                                                                       tuple2.getT1().getInteractiveId(),
                                                                                       message.getPathwayId()).produce();
                },
                ex -> {
                    logger.jsonError("Exception while broadcasting CreateInteractive Message",
                                 new HashMap<String, Object>() {
                                     {
                                         put("message", message.toString());
                                     }
                                 }, ex);
                    int statusCode = HttpStatus.SC_UNPROCESSABLE_ENTITY;
                    if (ex instanceof InteractiveAlreadyExistsFault) {
                        statusCode = HttpStatus.SC_CONFLICT;
                    }
                    Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_CREATE_ERROR, statusCode,
                            "Unable to create interactive");
                }
        );
    }
}
