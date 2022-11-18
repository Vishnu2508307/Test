package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.ReplaceInteractiveConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.InteractiveConfigChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReplaceInteractiveConfigMessageHandler implements MessageHandler<ReplaceInteractiveConfigMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ReplaceInteractiveConfigMessageHandler.class);

    public static final String AUTHOR_INTERACTIVE_CONFIG_REPLACE = "author.interactive.config.replace";
    public static final String AUTHOR_INTERACTIVE_CONFIG_REPLACE_OK = "author.interactive.config.replace.ok";
    public static final String AUTHOR_INTERACTIVE_CONFIG_REPLACE_ERROR = "author.interactive.config.replace.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final InteractiveService interactiveService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final InteractiveConfigChangeRTMProducer interactiveConfigChangeRTMProducer;

    @Inject
    public ReplaceInteractiveConfigMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                                  InteractiveService interactiveService,
                                                  Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                  CoursewareService coursewareService,
                                                  Provider<RTMClientContext> rtmClientContextProvider,
                                                  InteractiveConfigChangeRTMProducer interactiveConfigChangeRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.interactiveService = interactiveService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.interactiveConfigChangeRTMProducer = interactiveConfigChangeRTMProducer;
    }

    @Override
    public void validate(ReplaceInteractiveConfigMessage message) throws RTMValidationException {
        try {
            affirmArgument(message.getInteractiveId() != null, "missing interactiveId");
            affirmArgumentNotNullOrEmpty(message.getConfig(), "missing config");
            interactiveService.findById(message.getInteractiveId()).block();

        } catch (IllegalArgumentException | InteractiveNotFoundException e) {
            logger.jsonDebug("Exception thrown while finding InteractiveId", new HashMap<String, Object>() {
                {
                    put("interactiveId", message.getInteractiveId());
                }
            });
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_INTERACTIVE_CONFIG_REPLACE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_CONFIG_REPLACE)
    public void handle(Session session, ReplaceInteractiveConfigMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<InteractiveConfig> replaceMono =
                interactiveService.replaceConfig(account.getId(), message.getInteractiveId(), message.getConfig())
                        .single()
                .flatMap(interactiveConfig -> {
                    return coursewareService.saveConfigurationFields(message.getInteractiveId(), message.getConfig())
                            .then(Mono.just(interactiveConfig));
                })
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getInteractiveId(), INTERACTIVE);
        Mono.zip(replaceMono, rootElementIdMono).subscribe(
                tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_INTERACTIVE_CONFIG_REPLACE_OK,
                            message.getId());
                    basicResponseMessage.addField("config", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage =
                            new CoursewareElementBroadcastMessage()
                                    .setAccountId(account.getId())
                                    .setElement(new CoursewareElement(message.getInteractiveId(), INTERACTIVE))
                                    .setAction(CoursewareAction.CONFIG_CHANGE);

                    rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_CONFIG_REPLACE, broadcastMessage);
                    interactiveConfigChangeRTMProducer.buildInteractiveConfigChangeRTMConsumable(rtmClientContext,
                                                                                                 tuple2.getT2(),
                                                                                                 message.getInteractiveId(),
                                                                                                 message.getConfig()).produce();
                },
                ex -> {
                    logger.debug("Unable to replace configuration  " , ex);
                    Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_CONFIG_REPLACE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to save configuration");
                }
        );
    }
}
