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
import com.smartsparrow.rtm.message.recv.courseware.AddAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.assetadded.AssetAddedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class AddAssetMessageHandler implements MessageHandler<AddAssetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AddAssetMessageHandler.class);

    public static final String AUTHOR_COURSEWARE_ASSET_ADD = "author.courseware.asset.add";
    public static final String AUTHOR_COURSEWARE_ASSET_ADD_OK = AUTHOR_COURSEWARE_ASSET_ADD + ".ok";
    public static final String AUTHOR_COURSEWARE_ASSET_ADD_ERROR = AUTHOR_COURSEWARE_ASSET_ADD + ".error";

    private final CoursewareAssetService coursewareAssetService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AssetAddedRTMProducer assetAddedRTMProducer;

    @Inject
    public AddAssetMessageHandler(CoursewareAssetService coursewareAssetService,
                                  CoursewareService coursewareService,
                                  Provider<RTMEventBroker> rtmEventBrokerProvider,
                                  AuthenticationContextProvider authenticationContextProvider,
                                  Provider<RTMClientContext> rtmClientContextProvider,
                                  AssetAddedRTMProducer assetAddedRTMProducer) {
        this.coursewareAssetService = coursewareAssetService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.assetAddedRTMProducer = assetAddedRTMProducer;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(AddAssetMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
            checkArgument(!Strings.isNullOrEmpty(message.getAssetURN()), "assetURN is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_COURSEWARE_ASSET_ADD_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COURSEWARE_ASSET_ADD)
    public void handle(Session session, AddAssetMessage message) {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        final Account account = authenticationContextProvider.get().getAccount();
        try {
            Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getElementId(), message.getElementType());
            rootElementIdMono.flatMapMany(rootElementId ->
                                           coursewareAssetService.addAsset(message.getElementId(),
                                                                           message.getElementType(),
                                                                           message.getAssetURN(),
                                                                           rootElementId)
                    ).then(rootElementIdMono)
                    .doOnEach(log.reactiveErrorThrowable("error while adding an asset to courseware",
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
                    .subscribe(rootElementId -> {
                        assetAddedRTMProducer.buildAssetAddedRTMConsumable(rtmClientContext,
                                                                           rootElementId,
                                                                           message.getElementId(),
                                                                           message.getElementType()).produce();
                    }, ex -> {
                        log.jsonDebug("Unable to add asset to courseware element", new HashMap<String, Object>() {
                            {
                                put("elementId", message.getElementId());
                                put("elementType", message.getElementType());
                                put("error", ex.getStackTrace());
                            }
                        });
                        Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSET_ADD_ERROR,
                                                HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to add asset");
                    }, () -> {
                        Responses.writeReactive(session,
                                                new BasicResponseMessage(AUTHOR_COURSEWARE_ASSET_ADD_OK,
                                                                         message.getId()));

                        CoursewareElementBroadcastMessage broadcastMessage = new CoursewareElementBroadcastMessage()
                                .setParentElement(null)
                                .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                                .setAccountId(account.getId())
                                .setAction(CoursewareAction.ASSET_ADDED);
                        rtmEventBroker.broadcast(AUTHOR_COURSEWARE_ASSET_ADD, broadcastMessage);
                    });
        } catch (AssetURNParseException ex) {
            log.warn("Unable to parse asset URN");
            Responses.errorReactive(session, message.getId(), AUTHOR_COURSEWARE_ASSET_ADD_ERROR,
                                    HttpStatus.SC_BAD_REQUEST, "Invalid asset URN");
        }
    }
}
