package com.smartsparrow.rtm.message.handler.ingestion;

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
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionCreateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;


public class ProjectIngestionCreateMessageHandler implements MessageHandler<ProjectIngestionCreateMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionCreateMessageHandler.class);

    public static final String PROJECT_INGESTION_CREATE = "project.ingest.create";
    public static final String PROJECT_INGESTION_CREATE_OK = "project.ingest.create.ok";
    public static final String PROJECT_INGESTION_CREATE_ERROR = "project.ingest.create.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final IngestionService ingestionService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final ActivityIngestionProducer activityIngestionRTMProducer;

    @Inject
    public ProjectIngestionCreateMessageHandler(final AuthenticationContextProvider authenticationContextProvider,
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
    public void validate(ProjectIngestionCreateMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "missing project id");
        affirmArgument(message.getWorkspaceId() != null, "missing workspace id");
        affirmArgument(message.getConfigFields() != null, "missing configFields");
        affirmArgument(message.getCourseName() != null, "missing courseName");
        affirmArgument(message.getFileName() != null, "missing fileName");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_CREATE)
    @Override
    public void handle(Session session, ProjectIngestionCreateMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        Account account = authenticationContextProvider.get().getAccount();
        ingestionService.create(message.getProjectId(), message.getWorkspaceId(),message.getConfigFields(), account.getId(), null,
                                message.getIngestStats(), message.getCourseName(), message.getRootElementId())
            .flatMap(ingestionSummary -> ingestionService.createSignedUrl(ingestionSummary.getId(), message.getFileName()))
            .doOnEach(logger.reactiveErrorThrowable("error creating the ingestionSummary"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(payload -> {
                Responses.writeReactive(session,
                                        new BasicResponseMessage(PROJECT_INGESTION_CREATE_OK, message.getId())
                                                .addField("ingestionId", payload.getIngestionId())
                                                .addField("url", payload.getSignedUrl()));
                ProjectBroadcastMessage broadcastMessage = new ProjectBroadcastMessage()
                        .setProjectId(message.getProjectId())
                        .setIngestionId(payload.getIngestionId())
                        .setIngestionStatus(IngestionStatus.UPLOADING);
                rtmEventBroker.broadcast(PROJECT_INGESTION_CREATE, broadcastMessage);
                activityIngestionRTMProducer.buildIngestionConsumable(payload.getIngestionId(),
                                                                                message.getProjectId(),
                                                                                 message.getRootElementId(),
                                                                                 IngestionStatus.UPLOADING).produce();
            }, ex -> {
                logger.jsonDebug("Unable to create ingestionSummary", new HashMap<String, Object>() {
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                if (ex instanceof ConflictFault) {
                    Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_CREATE_ERROR,
                                            HttpStatus.SC_CONFLICT, "error creating the ingestionSummary. Course name already exists");
                } else {
                    Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_CREATE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating the ingestionSummary");
                }
            });
    }
}
