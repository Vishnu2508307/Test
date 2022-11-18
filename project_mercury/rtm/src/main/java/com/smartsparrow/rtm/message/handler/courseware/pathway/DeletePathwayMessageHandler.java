package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.DeletePathwayMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.PathwayDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeletePathwayMessageHandler implements MessageHandler<DeletePathwayMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeletePathwayMessageHandler.class);

    public static final String AUTHOR_PATHWAY_DELETE = "author.pathway.delete";
    private static final String AUTHOR_PATHWAY_DELETE_OK = "author.pathway.delete.ok";
    private static final String AUTHOR_PATHWAY_DELETE_ERROR = "author.pathway.delete.error";

    private final PathwayService pathwayService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final PathwayDeletedRTMProducer pathwayDeletedRTMProducer;

    @Inject
    public DeletePathwayMessageHandler(PathwayService pathwayService,
                                       CoursewareService coursewareService,
                                       Provider<RTMEventBroker> rtmEventBrokerProvider,
                                       AuthenticationContextProvider authenticationContextProvider,
                                       Provider<RTMClientContext> rtmClientContextProvider,
                                       PathwayDeletedRTMProducer pathwayDeletedRTMProducer) {
        this.pathwayService = pathwayService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.pathwayDeletedRTMProducer = pathwayDeletedRTMProducer;
    }

    @Override
    public void validate(DeletePathwayMessage message) throws RTMValidationException {

        try {
            checkArgument(message.getPathwayId() != null, "pathwayId is required");
            checkArgument(message.getParentActivityId() != null, "parentActivityId is required");
            UUID parentActivityId = pathwayService.findParentActivityId(message.getPathwayId()).block();
            checkArgument(message.getParentActivityId().equals(parentActivityId),
                    "supplied parentActivityId does not match the pathway parent");
        } catch (IllegalArgumentException | ParentActivityNotFoundException e) {
            log.debug("Error while deleting the pathway {} ",  new HashMap<String,Object>(){
                {
                   put("messageId",message.getId());
                   put("message",message.toString());
                   put("errorCode",AUTHOR_PATHWAY_DELETE_ERROR);
                   put("Message", e.getMessage());
                }
            });
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_PATHWAY_DELETE_ERROR);
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PATHWAY_DELETE)
    @Override
    public void handle(Session session, DeletePathwayMessage message) throws WriteResponseException {

        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        pathwayService.delete(message.getPathwayId(), message.getParentActivityId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getParentActivityId(), ACTIVITY))
                .subscribe(rootElementId -> {
                    pathwayDeletedRTMProducer.buildPathwayDeletedRTMConsumable(rtmClientContext,
                                                                               rootElementId,
                                                                               message.getPathwayId()).produce();
                }, ex -> {
                    log.debug("error deleting pathway for {} ",  new HashMap<String,Object>() {
                        {
                            put("messageId",message.getId());
                            put("message", message.toString());
                            put("errorCode",AUTHOR_PATHWAY_DELETE_ERROR);
                            put("Exception", ex);
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_PATHWAY_DELETE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting pathway");
                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_PATHWAY_DELETE_OK, message.getId())
                            .addField("pathwayId", message.getPathwayId())
                            .addField("parentActivityId", message.getParentActivityId()));

                    CoursewareElementBroadcastMessage broadcastMessage = getData(message, account);

                    rtmEventBroker.broadcast(message.getType(), broadcastMessage);
                });
    }

    private CoursewareElementBroadcastMessage getData(DeletePathwayMessage message, Account account) {
        return new CoursewareElementBroadcastMessage()
                .setAction(CoursewareAction.DELETED)
                .setParentElement(CoursewareElement.from(message.getParentActivityId(), ACTIVITY))
                .setElement(CoursewareElement.from(message.getPathwayId(), PATHWAY))
                .setAccountId(account.getId());
    }
}
