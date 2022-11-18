package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareEvaluableMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.evaluableset.EvaluableSetRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

public class CoursewareEvaluableMessageHandler implements MessageHandler<CoursewareEvaluableMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareEvaluableMessageHandler.class);

    public static final String AUTHOR_EVALUABLE_SET = "author.evaluable.set";
    public static final String AUTHOR_EVALUABLE_SET_OK = "author.evaluable.set.ok";
    public static final String AUTHOR_EVALUABLE_SET_ERROR = "author.evaluable.set.error";

    private final WalkableService walkableService;
    private final CoursewareService coursewareService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final EvaluableSetRTMProducer evaluableSetRTMProducer;

    @Inject
    public CoursewareEvaluableMessageHandler(final WalkableService walkableService,
                                             final CoursewareService coursewareService,
                                             final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                             final Provider<AuthenticationContext> authenticationContextProvider,
                                             final Provider<RTMClientContext> rtmClientContextProvider,
                                             final EvaluableSetRTMProducer evaluableSetRTMProducer) {
        this.walkableService = walkableService;
        this.coursewareService = coursewareService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.evaluableSetRTMProducer = evaluableSetRTMProducer;
    }

    @Override
    public void validate(final CoursewareEvaluableMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getEvaluationMode() != null, "missing evaluationMode");
    }

    @Override
    public void handle(final Session session, final CoursewareEvaluableMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        Account account = authenticationContextProvider.get().getAccount();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        walkableService.updateEvaluationMode(message.getElementId(),
                                             message.getElementType(),
                                             message.getEvaluationMode())
                .doOnEach(log.reactiveErrorThrowable("error while creating courseware evaluable",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .then(coursewareService.getRootElementId(message.getElementId(), message.getElementType()))
                .subscribe(rootElementId -> {
                    evaluableSetRTMProducer.buildEvaluableSetRTMConsumable(rtmClientContext,
                                                                           rootElementId,
                                                                           message.getElementId(),
                                                                           message.getElementType(),
                                                                           message.getEvaluationMode()).produce();
                }, ex -> {
                    log.jsonError("could not create evaluable", new HashMap<>(), ex);
                    Responses.errorReactive(session, message.getId(), AUTHOR_EVALUABLE_SET_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not create evaluable");
                }, () -> {
                    // use a map, so we don't change the return fields that might cause breaking changes
                    Map<String, Object> evaluable = new HashMap<>();
                    evaluable.put("elementId", message.getElementId());
                    evaluable.put("elementType", message.getElementType());
                    evaluable.put("evaluationMode", message.getEvaluationMode());
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_EVALUABLE_SET_OK, message.getId())
                            .addField("evaluable", evaluable));
                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                            .setAccountId(account.getId())
                            .setAction(CoursewareAction.EVALUABLE_SET);
                    rtmEventBroker.broadcast(AUTHOR_EVALUABLE_SET, broadcastMessage);
                });
    }
}
