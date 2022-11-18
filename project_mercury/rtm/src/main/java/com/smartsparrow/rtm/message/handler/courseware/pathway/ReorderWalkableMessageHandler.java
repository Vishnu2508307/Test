package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.ReorderWalkableMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.pathwayreordered.PathwayReOrderedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ReorderWalkableMessageHandler implements MessageHandler<ReorderWalkableMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ReorderWalkableMessageHandler.class);

    public static final String AUTHOR_WALKABLE_REORDER = "author.pathway.walkable.reorder";
    public static final String AUTHOR_WALKABLE_REORDER_OK = "author.pathway.walkable.reorder.ok";
    public static final String AUTHOR_WALKABLE_REORDER_ERROR = "author.pathway.walkable.reorder.error";

    private final PathwayService pathwayService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PathwayReOrderedRTMProducer pathwayReOrderedRTMProducer;

    @Inject
    public ReorderWalkableMessageHandler(PathwayService pathwayService,
                                         CoursewareService coursewareService,
                                         Provider<RTMEventBroker> rtmEventBrokerProvider,
                                         AuthenticationContextProvider authenticationContextProvider,
                                         Provider<RTMClientContext> rtmClientContextProvider,
                                         PathwayReOrderedRTMProducer pathwayReOrderedRTMProducer) {
        this.pathwayService = pathwayService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pathwayReOrderedRTMProducer = pathwayReOrderedRTMProducer;
    }

    @Override
    public void validate(ReorderWalkableMessage message) throws RTMValidationException {
        affirmNotNull(message.getPathwayId(), "pathwayId is required");
        affirmArgumentNotNullOrEmpty(message.getWalkableIds(), "walkableIds is required and can not be empty");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_WALKABLE_REORDER)
    @Override
    public void handle(Session session, ReorderWalkableMessage message) {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        pathwayService.reorder(message.getPathwayId(), message.getWalkableIds())
                .next()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(Mono.zip(coursewareService.getRootElementId(message.getPathwayId(), PATHWAY),
                               pathwayService.getOrderedWalkableChildren(message.getPathwayId())))
                .subscribe(tuple2 -> {
                    pathwayReOrderedRTMProducer.buildPathwayReOrderedRTMConsumable(rtmClientContext,
                                                                                   tuple2.getT1(),
                                                                                   message.getPathwayId(),
                                                                                   tuple2.getT2()).produce();
                }, ex -> {
                    //on exception
                    log.jsonDebug("Author Walkable Reorder Error", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("errorCode", AUTHOR_WALKABLE_REORDER_ERROR);
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_WALKABLE_REORDER_ERROR, ex);
                }, () -> {
                    //on complete
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_WALKABLE_REORDER_OK,
                                                                                         message.getId());
                    Responses.writeReactive(session, basicResponseMessage);

                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setAccountId(account.getId())
                            .setAction(CoursewareAction.PATHWAY_REORDERED)
                            .setElement(CoursewareElement.from(message.getPathwayId(), PATHWAY));

                    rtmEventBroker.broadcast(AUTHOR_WALKABLE_REORDER, broadcastMessage);
                });
    }
}
