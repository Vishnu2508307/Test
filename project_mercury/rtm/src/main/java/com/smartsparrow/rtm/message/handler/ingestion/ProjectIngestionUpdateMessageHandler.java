package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionUpdateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;

public class ProjectIngestionUpdateMessageHandler implements MessageHandler<IngestionUpdateMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionUpdateMessageHandler.class);

    public static final String PROJECT_INGESTION_UPDATE = "project.ingest.update";
    public static final String PROJECT_INGESTION_UPDATE_OK = "project.ingest.update.ok";
    public static final String PROJECT_INGESTION_UPDATE_ERROR = "project.ingest.update.error";

    private final IngestionService ingestionService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityIngestionProducer activityIngestionRTMProducer;

    @Inject
    public ProjectIngestionUpdateMessageHandler(final IngestionService ingestionService,
                                                final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                final Provider<RTMClientContext> rtmClientContextProvider,
                                                final ActivityIngestionProducer activityIngestionRTMProducer) {
        this.ingestionService = ingestionService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityIngestionRTMProducer = activityIngestionRTMProducer;
    }

    @Override
    public void validate(IngestionUpdateMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "missing ingestion id");
        affirmArgument(message.getProjectId() != null, "missing project id");
        affirmArgument(message.getStatus() != null, "missing status");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, IngestionUpdateMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        ingestionService.updateIngestionStatus(message.getIngestionId(), message.getProjectId(), message.getStatus())
            .doOnEach(logger.reactiveErrorThrowable("error updating the ingestionRequest"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(summary -> {
                Responses.writeReactive(session, new BasicResponseMessage(PROJECT_INGESTION_UPDATE_OK, message.getId()));
                ProjectBroadcastMessage broadcastMessage = new ProjectBroadcastMessage()
                        .setProjectId(summary.getProjectId())
                        .setIngestionId(message.getIngestionId())
                        .setIngestionStatus(summary.getStatus());
                rtmEventBroker.broadcast(PROJECT_INGESTION_UPDATE, broadcastMessage);
                activityIngestionRTMProducer.buildIngestionConsumable(message.getIngestionId(),
                                                                                message.getProjectId(),
                                                                                 summary.getRootElementId(),
                                                                                 summary.getStatus()).produce();
            }, ex -> {
                logger.debug("error updating the ingestionRequest", new HashMap<String, Object>() {
                    {
                        put("ingestionId", message.getIngestionId());
                        put("id", message.getId());
                        put("error", ex.getStackTrace());
                    }
                });

                Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_UPDATE_ERROR,
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY, "error updating the ingestionRequest");
            });
    }
}
