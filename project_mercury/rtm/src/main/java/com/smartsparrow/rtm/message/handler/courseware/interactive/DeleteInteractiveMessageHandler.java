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

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.DeleteInteractiveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.deleted.InteractiveDeletedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;


public class DeleteInteractiveMessageHandler implements MessageHandler<DeleteInteractiveMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteInteractiveMessage.class);

    public static final String AUTHOR_INTERACTIVE_DELETE = "author.interactive.delete";
    public static final String AUTHOR_INTERACTIVE_DELETE_OK = "author.interactive.delete.ok";
    public static final String AUTHOR_INTERACTIVE_DELETE_ERROR = "author.interactive.delete.error";

    private final InteractiveService interactiveService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final InteractiveDeletedRTMProducer interactiveDeletedRTMProducer;

    @Inject
    public DeleteInteractiveMessageHandler(InteractiveService interactiveService,
                                           CoursewareService coursewareService,
                                           Provider<RTMEventBroker> rtmEventBrokerProvider,
                                           AuthenticationContextProvider authenticationContextProvider,
                                           Provider<RTMClientContext> rtmClientContextProvider,
                                           InteractiveDeletedRTMProducer interactiveDeletedRTMProducer) {
        this.interactiveService = interactiveService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.interactiveDeletedRTMProducer = interactiveDeletedRTMProducer;
    }

    /**
     * Validate that all the message field are supplied and valid. It also validates that the supplied parentPathwayId
     * is in fact the valid parent of the supplied interactive id
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the validation fails
     */
    @Override
    public void validate(DeleteInteractiveMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getInteractiveId() != null, "interactiveId is required");
            checkArgument(message.getParentPathwayId() != null, "parentPathwayId is required");

            UUID parentPathwayId = interactiveService.findParentPathwayId(message.getInteractiveId()).block();
            checkArgument(message.getParentPathwayId().equals(parentPathwayId),
                    "supplied parentPathwayId does not match the interactive parent");
        } catch (Exception exception) {
            log.jsonDebug("Exception while Deleting Interactive Message", new HashMap<String, Object>() {
                {
                    put("message", message.toString());
                }
            });
            throw new RTMValidationException(exception.getMessage(), message.getId(), AUTHOR_INTERACTIVE_DELETE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_INTERACTIVE_DELETE)
    public void handle(Session session, DeleteInteractiveMessage message) {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        interactiveService.delete(message.getInteractiveId(), message.getParentPathwayId())
                .doOnEach(log.reactiveErrorThrowable("Error while deleting an Interactive",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("message", message.toString());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .then(coursewareService.getRootElementId(message.getParentPathwayId(), PATHWAY))
                .subscribe(rootElementId -> {
                               interactiveDeletedRTMProducer.buildInteractiveDeletedRTMConsumable(rtmClientContext,
                                                                                                  rootElementId,
                                                                                                  message.getInteractiveId(),
                                                                                                  message.getParentPathwayId()).produce();
                           },
                           ex -> emitError(session, message, ex),
                           () -> {
                               emitSuccess(session, message);
                               broadcastEvent(rtmEventBroker, message, account);
                           });
    }

    private void emitSuccess(Session session, DeleteInteractiveMessage message) {
        Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_INTERACTIVE_DELETE_OK, message.getId())
                .addField("interactiveId", message.getInteractiveId())
                .addField("parentPathwayId", message.getParentPathwayId()));
    }

    private void emitError(Session session, DeleteInteractiveMessage message, Throwable ex) {
        ex = Exceptions.unwrap(ex);
        int status = ex instanceof InteractiveNotFoundException ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_UNPROCESSABLE_ENTITY;
        log.jsonDebug("Interactive Message Not Found with ID", new HashMap<String, Object>() {
            {
                put("interactiveId", message.getInteractiveId());
            }
        });
       Responses.errorReactive(session, message.getId(), AUTHOR_INTERACTIVE_DELETE_ERROR,
                status, ex.getMessage());
    }

    private void broadcastEvent(RTMEventBroker rtmEventBroker, DeleteInteractiveMessage message, Account account) {
        CoursewareElementBroadcastMessage broadcastMessage =
                new CoursewareElementBroadcastMessage()
                        .setParentElement(new CoursewareElement(message.getParentPathwayId(), PATHWAY))
                        .setElement(new CoursewareElement(message.getInteractiveId(), INTERACTIVE))
                        .setAction(CoursewareAction.DELETED)
                        .setAccountId(account.getId());

        rtmEventBroker.broadcast(AUTHOR_INTERACTIVE_DELETE, broadcastMessage);
    }

}
