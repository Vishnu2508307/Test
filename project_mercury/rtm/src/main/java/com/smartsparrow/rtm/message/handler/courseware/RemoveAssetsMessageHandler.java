package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.RemoveAssetsMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.assetsremoved.AssetsRemovedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class RemoveAssetsMessageHandler implements MessageHandler<RemoveAssetsMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RemoveAssetsMessageHandler.class);

    public static final String AUTHOR_COURSEWARE_ASSETS_REMOVE = "author.courseware.assets.remove";
    public static final String AUTHOR_COURSEWARE_ASSETS_REMOVE_OK = AUTHOR_COURSEWARE_ASSETS_REMOVE + ".ok";
    public static final String AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR = AUTHOR_COURSEWARE_ASSETS_REMOVE + ".error";

    private final CoursewareAssetService coursewareAssetService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AssetsRemovedRTMProducer assetsRemovedRTMProducer;

    @Inject
    public RemoveAssetsMessageHandler(CoursewareAssetService coursewareAssetService,
                                      Provider<RTMEventBroker> rtmEventBrokerProvider,
                                      AuthenticationContextProvider authenticationContextProvider,
                                      CoursewareService coursewareService,
                                      Provider<RTMClientContext> rtmClientContextProvider,
                                      AssetsRemovedRTMProducer assetsRemovedRTMProducer) {
        this.coursewareAssetService = coursewareAssetService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.assetsRemovedRTMProducer = assetsRemovedRTMProducer;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(RemoveAssetsMessage message) {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
        affirmArgument(message.getAssetURN() != null && !message.getAssetURN().isEmpty(), "assetURN is required");
    }
    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, RemoveAssetsMessage message) {
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getElementId(),
                                                                          message.getElementType());
        rootElementIdMono.flatMap(rootElementId ->
                                          coursewareAssetService.removeAssets(message.getElementId(),
                                                                              message.getAssetURN(),
                                                                              rootElementId)
                                                  .singleOrEmpty())
                .then(rootElementIdMono)
                .doOnEach(log.reactiveErrorThrowable("error deleting asset",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                             put("elementType", message.getElementType());
                                                         }
                                                     }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(rootElementId -> {
                    assetsRemovedRTMProducer.buildAssetsRemovedRTMConsumable(rtmClientContext,
                                                                             rootElementId,
                                                                             message.getElementId(),
                                                                             message.getElementType()).produce();
                }, ex -> {
                    if(ex instanceof  AssetURNParseException){
                        Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR,
                                                HttpStatus.SC_BAD_REQUEST, "Invalid asset URN");
                    }else {
                        log.jsonError("Unable to remove asset from courseware element", new HashMap<String, Object>() {
                            {
                                put("elementId", message.getElementId());
                                put("elementType", message.getElementType());
                                put("error", ex.getStackTrace());
                            }
                        }, ex);
                        Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR,
                                                HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to remove assets");
                    }
                }, () -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_COURSEWARE_ASSETS_REMOVE_OK,
                                                                     message.getId()));

                    CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                            .setParentElement(null)
                            .setAccountId(account.getId())
                            .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                            .setAction(CoursewareAction.ASSETS_REMOVED);
                    rtmEventBroker.broadcast(AUTHOR_COURSEWARE_ASSETS_REMOVE, broadcastMessage);
                });
    }
}
