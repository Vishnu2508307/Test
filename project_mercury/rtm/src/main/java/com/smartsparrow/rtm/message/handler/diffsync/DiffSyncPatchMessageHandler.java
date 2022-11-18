package com.smartsparrow.rtm.message.handler.diffsync;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

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
import com.smartsparrow.rtm.message.recv.diffsync.DiffSyncPatchMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncIdentifierType;
import data.DiffSyncService;
import data.Message;


public class DiffSyncPatchMessageHandler implements MessageHandler<DiffSyncPatchMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncPatchMessageHandler.class);

    public static final String DIFF_SYNC_PATCH = "diff.sync.patch";
    public static final String DIFF_SYNC_PATCH_OK = "diff.sync.patch.ok";
    public static final String DIFF_SYNC_PATCH_ERROR = "diff.sync.patch.error";

    private final DiffSyncService diffSyncService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ServerIdentifier serverIdentifier;
    private final DiffSyncProducer diffSyncProducer;

    @Inject
    public DiffSyncPatchMessageHandler(final DiffSyncService diffSyncService,
                                       final Provider<RTMClientContext> rtmClientContextProvider,
                                       final ServerIdentifier serverIdentifier,
                                       final DiffSyncProducer diffSyncProducer) {
        this.diffSyncService = diffSyncService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.serverIdentifier = serverIdentifier;
        this.diffSyncProducer = diffSyncProducer;
    }

    @Override
    public void validate(DiffSyncPatchMessage message) throws RTMValidationException {
        affirmArgument(message.getEntityType() != null, "missing entity type");
        affirmArgument(message.getEntityId() != null, "missing entityId");
        affirmArgumentNotNullOrEmpty(message.getPatches(), "missing patches");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = DIFF_SYNC_PATCH)
    @Override
    public void handle(Session session,
                       DiffSyncPatchMessage message) throws WriteResponseException {

        final String clientId = rtmClientContextProvider.get().getClientId();

        //create entity info
        DiffSyncEntity diffSyncEntity = new DiffSyncEntity().setEntityId(message.getEntityId())
                .setEntityType(message.getEntityType());

        // create unique identifier
        DiffSyncIdentifier diffSyncIdentifier = new DiffSyncIdentifier()
                .setClientId(clientId)
                .setServerId(serverIdentifier.getServerId())
                .setType(DiffSyncIdentifierType.CLIENT);

        // diff sync patch
        diffSyncService.syncPatch(diffSyncEntity,
                                  message.getPatches(),
                                  diffSyncIdentifier)
                .map(patch -> {
                    // send event through publisher
                    diffSyncProducer.buildConsumableMessage(Message.build(patch),
                                                            diffSyncIdentifier,
                                                            diffSyncEntity).produce();
                    return patch;
                })
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("error in diff sync patch",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("entity type", message.getEntityType());
                                                         }
                                                     }))
                .subscribe(patch -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       DIFF_SYNC_PATCH_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonError("Unable to diff sync patch {}",
                                             new HashMap<String, Object>() {
                                                 {
                                                     put("message", message.toString());
                                                     put("error", ex.getStackTrace());
                                                 }
                                             },
                                             ex);
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       DIFF_SYNC_PATCH_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to diff sync patch");
                           }
                );
    }
}
