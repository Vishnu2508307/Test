package com.smartsparrow.rtm.message.handler.courseware;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.RemoveAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.assetremoved.AssetRemovedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class RemoveAssetMessageHandler implements MessageHandler<RemoveAssetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RemoveAssetMessageHandler.class);

    public static final String AUTHOR_COURSEWARE_ASSET_REMOVE = "author.courseware.asset.remove";
    public static final String AUTHOR_COURSEWARE_ASSET_REMOVE_OK = AUTHOR_COURSEWARE_ASSET_REMOVE + ".ok";
    public static final String AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR = AUTHOR_COURSEWARE_ASSET_REMOVE + ".error";

    private final CoursewareAssetService coursewareAssetService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AssetRemovedRTMProducer assetRemovedRTMProducer;

    @Inject
    public RemoveAssetMessageHandler(CoursewareAssetService coursewareAssetService,
                                     Provider<RTMEventBroker> rtmEventBrokerProvider,
                                     AuthenticationContextProvider authenticationContextProvider,
                                     CoursewareService coursewareService,
                                     Provider<RTMClientContext> rtmClientContextProvider,
                                     AssetRemovedRTMProducer assetRemovedRTMProducer) {
        this.coursewareAssetService = coursewareAssetService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.assetRemovedRTMProducer = assetRemovedRTMProducer;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(RemoveAssetMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
            checkArgument(!Strings.isNullOrEmpty(message.getAssetURN()), "assetURN is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COURSEWARE_ASSET_REMOVE)
    public void handle(Session session, RemoveAssetMessage message) {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        try {
            Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getElementId(),
                                                                              message.getElementType());
            rootElementIdMono.flatMap(rootElementId ->
                                              coursewareAssetService.removeAsset(message.getElementId(),
                                                                                 message.getAssetURN(),
                                                                                 rootElementId)
                                                      .singleOrEmpty())
                    .then(rootElementIdMono)
                    .doOnEach(log.reactiveErrorThrowable("error deleting asset",
                                                         throwable -> new HashMap<String, Object>() {
                                                             {
                                                                 put("elementId", message.getElementId());
                                                                 put("elementType", message.getElementType());
                                                                 put("assetURN", message.getAssetURN());
                                                             }
                                                         }))
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .subscribe(rootId -> {
                        assetRemovedRTMProducer.buildAssetRemovedRTMConsumable(rtmClientContext,
                                                                               rootId,
                                                                               message.getElementId(),
                                                                               message.getElementType()).produce();
                    }, ex -> {
                        log.jsonDebug("Unable to remove asset from courseware element", new HashMap<String, Object>() {
                            {
                                put("elementId", message.getElementId());
                                put("elementType", message.getElementType());
                                put("error", ex.getStackTrace());
                            }
                        });
                        Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR,
                                                HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to remove asset");
                    }, () -> {
                        Responses.writeReactive(session,
                                                new BasicResponseMessage(AUTHOR_COURSEWARE_ASSET_REMOVE_OK,
                                                                         message.getId()));

                        CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                .setParentElement(null)
                                .setAccountId(account.getId())
                                .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                                .setAction(CoursewareAction.ASSET_REMOVED);
                        rtmEventBroker.broadcast(AUTHOR_COURSEWARE_ASSET_REMOVE, broadcastMessage);
                    });
        } catch (AssetURNParseException ex) {
            log.warn("Unable to parse asset URN");
            Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR,
                                    HttpStatus.SC_BAD_REQUEST, "Invalid asset URN");
        }
    }
}
