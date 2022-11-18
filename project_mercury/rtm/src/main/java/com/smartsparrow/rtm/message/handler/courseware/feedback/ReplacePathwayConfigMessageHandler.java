package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.handler.courseware.component.ReplaceComponentConfigMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.pathway.ReplacePathwayConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.PathwayConfigChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReplacePathwayConfigMessageHandler implements MessageHandler<ReplacePathwayConfigMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReplaceComponentConfigMessageHandler.class);

    public static final String AUTHOR_PATHWAY_CONFIG_REPLACE = "author.pathway.config.replace";
    private static final String AUTHOR_PATHWAY_CONFIG_REPLACE_OK = "author.pathway.config.replace.ok";
    private static final String AUTHOR_PATHWAY_CONFIG_REPLACE_ERROR = "author.pathway.config.replace.error";

    private final PathwayService pathwayService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PathwayConfigChangeRTMProducer pathwayConfigChangeRTMProducer;

    @Inject
    public ReplacePathwayConfigMessageHandler(PathwayService pathwayService,
                                              CoursewareService coursewareService,
                                              Provider<RTMEventBroker> rtmEventBrokerProvider,
                                              AuthenticationContextProvider authenticationContextProvider,
                                              Provider<RTMClientContext> rtmClientContextProvider,
                                              PathwayConfigChangeRTMProducer pathwayConfigChangeRTMProducer) {
        this.pathwayService = pathwayService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pathwayConfigChangeRTMProducer = pathwayConfigChangeRTMProducer;
    }

    @Override
    public void validate(ReplacePathwayConfigMessage message) throws RTMValidationException {
        affirmArgument(message.getPathwayId() != null, "pathwayId is required");
        affirmArgument(!StringUtils.isBlank(message.getConfig()), "config is required");

        try {
            pathwayService.findById(message.getPathwayId())
                    .doOnEach(log.reactiveErrorThrowable("Error occurred while fetching pathway"))
                    .block();
        } catch (PathwayNotFoundException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_PATHWAY_CONFIG_REPLACE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PATHWAY_CONFIG_REPLACE)
    @Override
    public void handle(Session session, ReplacePathwayConfigMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        Mono<PathwayConfig> pathwayConfigMono = pathwayService.replaceConfig(message.getPathwayId(),
                                                                             message.getConfig())
                .doOnEach(log.reactiveErrorThrowable("Error occurred while replacing pathway configuration"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getPathwayId(), PATHWAY);
        Mono.zip(pathwayConfigMono, rootElementIdMono)
                .subscribe(tuple2 -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                            AUTHOR_PATHWAY_CONFIG_REPLACE_OK,
                            message.getId());
                    basicResponseMessage.addField("config", tuple2.getT1());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage =
                            new CoursewareElementBroadcastMessage()
                                    .setAccountId(account.getId())
                                    .setElement(new CoursewareElement(message.getPathwayId(), PATHWAY))
                                    .setAction(CoursewareAction.CONFIG_CHANGE);

                    rtmEventBroker.broadcast(AUTHOR_PATHWAY_CONFIG_REPLACE, broadcastMessage);
                    pathwayConfigChangeRTMProducer.buildPathwayConfigChangeRTMConsumable(rtmClientContext,
                                                                                         tuple2.getT2(),
                                                                                         message.getPathwayId(),
                                                                                         message.getConfig()).produce();
                }, ex -> {
                    log.debug("Unable to replace configuration", new HashMap<String, Object>() {
                        {
                            put("pathwayId", message.getPathwayId());
                            put("config", message.getConfig());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), AUTHOR_PATHWAY_CONFIG_REPLACE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to save configuration");
                });
    }
}
