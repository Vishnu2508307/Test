package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_CANCEL_REQUEST;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;

public class ProjectIngestionDeleteMessageHandler implements MessageHandler<IngestionGenericMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionDeleteMessageHandler.class);

    public static final String PROJECT_INGESTION_DELETE = "project.ingest.delete";
    public static final String PROJECT_INGESTION_DELETE_OK = "project.ingest.delete.ok";
    public static final String PROJECT_INGESTION_DELETE_ERROR = "project.ingest.delete.error";

    private final IngestionService ingestionService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityIngestionProducer activityIngestionRTMProducer;

    @Inject
    public ProjectIngestionDeleteMessageHandler(final IngestionService ingestionService,
                                                final Provider<RTMClientContext> rtmClientContextProvider,
                                                final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                final ActivityIngestionProducer activityIngestionRTMProducer) {
        this.ingestionService = ingestionService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityIngestionRTMProducer = activityIngestionRTMProducer;
    }

    @Override
    public void validate(IngestionGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "missing ingestion id");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, IngestionGenericMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        ingestionService.deleteById(message.getIngestionId())
            .flatMap(ingestionSummary -> ingestionService.publishToSQS(ingestionSummary, SUBMIT_INGESTION_CANCEL_REQUEST, ""))
            .doOnEach(logger.reactiveErrorThrowable("error deleting the ingestionRequest"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(summary -> {
                Responses.writeReactive(session,
                                        new BasicResponseMessage(PROJECT_INGESTION_DELETE_OK, message.getId()));
                ProjectBroadcastMessage broadcastMessage = new ProjectBroadcastMessage()
                        .setProjectId(summary.getProjectId())
                        .setIngestionId(message.getIngestionId())
                        .setIngestionStatus(IngestionStatus.DELETED);
                rtmEventBroker.broadcast(PROJECT_INGESTION_DELETE, broadcastMessage);
                activityIngestionRTMProducer.buildIngestionConsumable(message.getIngestionId(),
                                                                                 summary.getProjectId(),
                                                                                 summary.getRootElementId(),
                                                                                 IngestionStatus.DELETED).produce();
            }, ex -> {
                logger.jsonDebug("Unable to delete ingestionRequest", new HashMap<String, Object>() {
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_DELETE_ERROR,
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting the ingestionRequest");
            });
    }
}
