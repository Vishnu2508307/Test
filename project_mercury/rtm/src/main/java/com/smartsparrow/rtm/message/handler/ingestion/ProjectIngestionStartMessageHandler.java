package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_ADAPTER_DOCX_REQUEST;
import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST;
import static com.smartsparrow.ingestion.route.IngestionRoute.SUBMIT_INGESTION_AMBROSIA_REQUEST;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.ingestion.data.IngestionAdapterType;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionStartMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class ProjectIngestionStartMessageHandler implements MessageHandler<ProjectIngestionStartMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionStartMessageHandler.class);

    public static final String PROJECT_INGESTION_START = "project.ingest.start";
    public static final String PROJECT_INGESTION_START_OK = "project.ingest.start.ok";
    public static final String PROJECT_INGESTION_START_ERROR = "project.ingest.start.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final IngestionService ingestionService;

    @Inject
    public ProjectIngestionStartMessageHandler(final AuthenticationContextProvider authenticationContextProvider,
                                               final IngestionService ingestionService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.ingestionService = ingestionService;
    }

    @Override
    public void validate(ProjectIngestionStartMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "missing ingestion id");
        affirmArgument(message.getAdapterType() != null, "missing adapter type");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_START)
    @Override
    public void handle(Session session, ProjectIngestionStartMessage message) throws WriteResponseException {
        String bearerToken = authenticationContextProvider.get().getWebToken().getToken();

        ingestionService.findById(message.getIngestionId())
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format(
                        "cannot find ingestionSummary by id: %s",
                        message.getIngestionId()))))
                .flatMap(ingestionSummary -> {
                    String adapterQueue = getAdapterQueue(message.getAdapterType());
                    return ingestionService.publishToSQS(ingestionSummary,
                            adapterQueue,
                            bearerToken);
                })
                .doOnEach(logger.reactiveErrorThrowable("error starting the ingestionRequest"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(summary -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(PROJECT_INGESTION_START_OK, message.getId())
                                                    .addField("ingestionId", summary.getId()));
                }, ex -> {
                    logger.jsonDebug("Unable to start ingestionRequest", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_START_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error starting the ingestionRequest");
                });
    }

    private String getAdapterQueue(IngestionAdapterType adapterType) {
        String adapterQueue = "";

        switch(adapterType) {
                // if Ambrosia it'll skip adapter straight to ambrosia step
            case AMBROSIA:
                adapterQueue = SUBMIT_INGESTION_AMBROSIA_REQUEST;
                break;
            case DOCX:
                adapterQueue = SUBMIT_INGESTION_ADAPTER_DOCX_REQUEST;
                break;
                // Default to EPUB so there's no break during transition from it being only adapter
            case EPUB:
            default:
                adapterQueue = SUBMIT_INGESTION_ADAPTER_EPUB_REQUEST;
        };

        return adapterQueue;
    };
}
