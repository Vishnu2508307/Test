package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.diffsync.DiffSyncProducer;
import com.smartsparrow.data.ServerIdentifier;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncAckMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import data.Ack;
import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncIdentifierType;
import data.DiffSyncService;
import data.Message;


public class DiffSyncAckMessageHandler implements MessageHandler<DiffSyncAckMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncAckMessageHandler.class);

    public static final String DIFF_SYNC_ACK = "diff.sync.ack";
    public static final String DIFF_SYNC_ACK_OK = "diff.sync.ack.ok";
    public static final String DIFF_SYNC_ACK_ERROR = "diff.sync.ack.error";

    private final DiffSyncService diffSyncService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ServerIdentifier serverIdentifier;
    private final DiffSyncProducer diffSyncProducer;

    @Inject
    public DiffSyncAckMessageHandler(final DiffSyncService diffSyncService,
                                     final Provider<RTMClientContext> rtmClientContextProvider,
                                     final ServerIdentifier serverIdentifier,
                                     final DiffSyncProducer diffSyncProducer) {
        this.diffSyncService = diffSyncService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.serverIdentifier = serverIdentifier;
        this.diffSyncProducer = diffSyncProducer;
    }

    @Override
    public void validate(DiffSyncAckMessage message) throws RTMValidationException {
        affirmArgument(message.getEntityType() != null, "missing entity type");
        affirmArgument(message.getEntityId() != null, "missing entityId");
        affirmArgument(message.getN() != null, "missing n version");
        affirmArgument(message.getM() != null, "missing m version");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = DIFF_SYNC_ACK)
    @Override
    public void handle(Session session,
                       DiffSyncAckMessage message) throws WriteResponseException {
        final String clientId = rtmClientContextProvider.get().getClientId();

        //create entity info
        DiffSyncEntity diffSyncEntity = new DiffSyncEntity().setEntityId(message.getEntityId())
                .setEntityType(message.getEntityType());

        // create unique identifier
        DiffSyncIdentifier diffSyncIdentifier = new DiffSyncIdentifier()
                .setClientId(clientId)
                .setServerId(serverIdentifier.getServerId())
                .setType(DiffSyncIdentifierType.CLIENT);

        // create ack request
        UUID ackId = UUIDs.timeBased();
        Ack ackRequest = new Ack().setId(ackId)
                .setClientId(clientId)
                .setM(message.getM())
                .setN(message.getN());
        // sync ack
        diffSyncService.syncAck(diffSyncEntity,
                                diffSyncIdentifier,
                                ackRequest)
                .doOnEach(log.reactiveErrorThrowable("error in diff sync ack",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("name", message.getEntityType());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ack -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       DIFF_SYNC_ACK_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);
                               diffSyncProducer.buildConsumableMessage(Message.build(ack),
                                                                       diffSyncIdentifier,
                                                                       diffSyncEntity).produce();
                           },
                           ex -> {
                               log.jsonError("Unable to diff sync ack {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       DIFF_SYNC_ACK_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to diff sync ack");
                           }
                );
    }
}
