package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.diffsync.DiffSyncProducer;
import com.smartsparrow.cache.diffsync.DiffSyncSubscription;
import com.smartsparrow.cache.diffsync.DiffSyncSubscriptionManager;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.diffsync.MessageTypeBridgeConverter;
import com.smartsparrow.rtm.diffsync.RTMChannelFactory;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncStartMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import data.Channel;
import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncIdentifierType;
import data.DiffSyncService;
import data.Message;
import data.Start;


public class DiffSyncStartMessageHandler implements MessageHandler<DiffSyncStartMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncStartMessageHandler.class);

    public static final String DIFF_SYNC_START = "diff.sync.start";
    public static final String DIFF_SYNC_START_OK = "diff.sync.start.ok";
    public static final String DIFF_SYNC_START_ERROR = "diff.sync.start.error";

    private final DiffSyncService diffSyncService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final MessageTypeBridgeConverter messageTypeBridgeConverter;
    private final DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory;
    private final Provider<DiffSyncSubscriptionManager> diffSyncSubscriptionManagerProvider;
    private final DiffSyncProducer diffSyncProducer;
    private final ServerIdentifier serverIdentifier;

    @Inject
    public DiffSyncStartMessageHandler(final DiffSyncService diffSyncService,
                                       final Provider<RTMClientContext> rtmClientContextProvider,
                                       final MessageTypeBridgeConverter messageTypeBridgeConverter,
                                       final DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory,
                                       final Provider<DiffSyncSubscriptionManager> diffSyncSubscriptionManagerProvider,
                                       final DiffSyncProducer diffSyncProducer,
                                       final ServerIdentifier serverIdentifier) {
        this.diffSyncService = diffSyncService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.messageTypeBridgeConverter = messageTypeBridgeConverter;
        this.diffSyncSubscriptionFactory = diffSyncSubscriptionFactory;
        this.diffSyncSubscriptionManagerProvider = diffSyncSubscriptionManagerProvider;
        this.diffSyncProducer = diffSyncProducer;
        this.serverIdentifier = serverIdentifier;
    }

    @Override
    public void validate(DiffSyncStartMessage message) throws RTMValidationException {
        affirmArgument(message.getEntityType() != null, "missing entity type");
        affirmArgument(message.getEntityId() != null, "missing entityId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = DIFF_SYNC_START)
    @Override
    public void handle(Session session,
                       DiffSyncStartMessage message) throws WriteResponseException {

        final DiffSyncSubscriptionManager diffSyncSubscriptionManager = diffSyncSubscriptionManagerProvider.get();

        final String clientId = rtmClientContextProvider.get().getClientId();

        //create entity info
        DiffSyncEntity diffSyncEntity = new DiffSyncEntity().setEntityId(message.getEntityId())
                .setEntityType(message.getEntityType());

        // create unique identifier
        DiffSyncIdentifier diffSyncIdentifier = new DiffSyncIdentifier()
                .setClientId(clientId)
                .setServerId(serverIdentifier.getServerId())
                .setType(DiffSyncIdentifierType.CLIENT);

        // create rtm channel
        Channel rtmChannel = new RTMChannelFactory(messageTypeBridgeConverter).create(session, clientId);
        //start the diff sync
        diffSyncService.start(diffSyncEntity,
                              rtmChannel,
                              diffSyncIdentifier)
                .map(diffSync -> {
                    //subscribe to the redis channel

                    DiffSyncSubscription diffSyncSubscription = diffSyncSubscriptionFactory.create(diffSyncEntity,
                                                                                                   diffSync.getDiffSyncIdentifier());

                    diffSyncSubscriptionManager.add(diffSyncSubscription).subscribe();
                    diffSyncProducer.buildConsumableMessage(Message.build(new Start()),
                                                            diffSyncIdentifier,
                                                            diffSyncEntity).produce();

                    return diffSync;
                })
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("error in diff sync start",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("name", message.getEntityType());
                                                         }
                                                     }))
                .subscribe(diffSync -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       DIFF_SYNC_START_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to diff sync start {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       DIFF_SYNC_START_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to diff sync start");
                           }
                );

    }
}
