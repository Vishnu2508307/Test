package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionRequestMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;

public class ProjectIngestionRequestMessageHandler implements MessageHandler<ProjectIngestionRequestMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionRequestMessageHandler.class);

    public static final String PROJECT_INGESTION_REQUEST = "project.ingest.request";
    public static final String PROJECT_INGESTION_REQUEST_OK = "project.ingest.request.ok";
    public static final String PROJECT_INGESTION_REQUEST_ERROR = "project.ingest.request.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final IngestionService ingestionService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityIngestionProducer activityIngestionRTMProducer;

    @Inject
    public ProjectIngestionRequestMessageHandler(final AuthenticationContextProvider authenticationContextProvider,
                                                 final IngestionService ingestionService,
                                                 final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                                 final Provider<RTMClientContext> rtmClientContextProvider,
                                                 final ActivityIngestionProducer activityIngestionRTMProducer) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.ingestionService = ingestionService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.activityIngestionRTMProducer = activityIngestionRTMProducer;
    }

    @Override
    public void validate(ProjectIngestionRequestMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "missing project id");
        affirmArgument(message.getWorkspaceId() != null, "missing workspace id");
        affirmArgument(message.getUrl() != null, "missing url");
        affirmArgument(!message.getUrl().isEmpty(), "url is empty");
        affirmArgument(message.getConfigFields() != null, "missing configFields");
        affirmArgument(message.getCourseName() != null, "missing courseName");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_REQUEST)
    @Override
    public void handle(Session session, ProjectIngestionRequestMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        Account account = authenticationContextProvider.get().getAccount();
        String bearerToken = authenticationContextProvider.get().getWebToken().getToken();

        ingestionService.create(message.getProjectId(), message.getWorkspaceId(), message.getConfigFields(), account.getId(), message.getUrl(),
                                message.getIngestStats(), message.getCourseName(), message.getRootElementId())
            .flatMap(ingestionSummary -> ingestionService.publishToSQS(ingestionSummary, SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST, bearerToken))
            .doOnEach(logger.reactiveErrorThrowable("error creating the ingestionRequest"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(summary -> {
                Responses.writeReactive(session,
                                        new BasicResponseMessage(PROJECT_INGESTION_REQUEST_OK, message.getId())
                                                .addField("ingestionId", summary.getId()));
                ProjectBroadcastMessage broadcastMessage = new ProjectBroadcastMessage()
                        .setProjectId(message.getProjectId())
                        .setIngestionId(summary.getId())
                        .setIngestionStatus(summary.getStatus());
                rtmEventBroker.broadcast(PROJECT_INGESTION_REQUEST, broadcastMessage);
                activityIngestionRTMProducer.buildIngestionConsumable(summary.getId(),
                                                                                message.getProjectId(),
                                                                                 message.getRootElementId(),
                                                                                 summary.getStatus()).produce();
            }, ex -> {
                logger.jsonDebug("Unable to create ingestionRequest", new HashMap<String, Object>() {
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                if (ex instanceof ConflictFault) {
                    Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_REQUEST_ERROR,
                                            HttpStatus.SC_CONFLICT, "error creating the ingestionRequest. Course name already exists");
                } else {
                    Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_REQUEST_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating the ingestionRequest");
                }
            });
    }
}
