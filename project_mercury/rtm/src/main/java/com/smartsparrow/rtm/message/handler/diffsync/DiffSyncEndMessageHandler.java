package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.diffsync.DiffSyncSubscription;
import com.smartsparrow.cache.diffsync.DiffSyncSubscriptionManager;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncEndMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import data.DiffSyncEntity;
import data.DiffSyncIdentifierType;
import data.DiffSyncIdentifier;
import data.DiffSyncService;


public class DiffSyncEndMessageHandler implements MessageHandler<DiffSyncEndMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncEndMessageHandler.class);

    public static final String DIFF_SYNC_END = "diff.sync.end";
    public static final String DIFF_SYNC_END_OK = "diff.sync.end.ok";
    public static final String DIFF_SYNC_END_ERROR = "diff.sync.end.error";

    private final DiffSyncService diffSyncService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory;
    private final Provider<DiffSyncSubscriptionManager> subscriptionManagerProvider;
    private final ServerIdentifier serverIdentifier;

    @Inject
    public DiffSyncEndMessageHandler(final DiffSyncService diffSyncService,
                                     final Provider<RTMClientContext> rtmClientContextProvider,
                                     final DiffSyncSubscription.DiffSyncSubscriptionFactory diffSyncSubscriptionFactory,
                                     final Provider<DiffSyncSubscriptionManager> subscriptionManagerProvider,
                                     final ServerIdentifier serverIdentifier) {
        this.diffSyncService = diffSyncService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.diffSyncSubscriptionFactory = diffSyncSubscriptionFactory;
        this.subscriptionManagerProvider = subscriptionManagerProvider;
        this.serverIdentifier = serverIdentifier;
    }

    @Override
    public void validate(DiffSyncEndMessage message) throws RTMValidationException {
        affirmArgument(message.getEntityType() != null, "missing entity type");
        affirmArgument(message.getEntityId() != null, "missing entityId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = DIFF_SYNC_END)
    @Override
    public void handle(Session session,
                       DiffSyncEndMessage message) throws WriteResponseException {

        final String clientId = rtmClientContextProvider.get().getClientId();

        //create entity info
        DiffSyncEntity diffSyncEntity = new DiffSyncEntity().setEntityId(message.getEntityId())
                .setEntityType(message.getEntityType());

        //unique identifier
        DiffSyncIdentifier diffSyncIdentifier = new DiffSyncIdentifier()
                .setClientId(clientId)
                .setServerId(serverIdentifier.getServerId())
                .setType(DiffSyncIdentifierType.CLIENT);

        //unsubscribe to the redis channel
        DiffSyncSubscription diffSyncSubscription = diffSyncSubscriptionFactory.create(diffSyncEntity,
                                                                                       diffSyncIdentifier);
        subscriptionManagerProvider.get().remove(diffSyncSubscription);

        // end diff sync
        diffSyncService.end(diffSyncEntity)
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("error in diff sync end",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("name", message.getEntityType());
                                                         }
                                                     }))
                .subscribe(ignore -> {
                               // nothing here, never executed
                           }, ex -> {
                               log.jsonDebug("Unable to end the diff sync", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session, message.getId(), DIFF_SYNC_END_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY, "error ending the diff sync");
                           },
                           () -> Responses.writeReactive(session,
                                                         new BasicResponseMessage(DIFF_SYNC_END_OK, message.getId()))
                );
    }
}
