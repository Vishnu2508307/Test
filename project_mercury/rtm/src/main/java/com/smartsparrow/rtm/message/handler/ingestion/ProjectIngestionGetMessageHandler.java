package com.smartsparrow.rtm.message.handler.ingestion;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ProjectIngestionGetMessageHandler implements MessageHandler<IngestionGenericMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionGetMessageHandler.class);

    public static final String PROJECT_INGESTION_GET = "project.ingest.get";
    public static final String PROJECT_INGESTION_GET_OK = "project.ingest.get.ok";
    public static final String PROJECT_INGESTION_GET_ERROR = "project.ingest.get.error";

    private final IngestionService ingestionService;

    @Inject
    public ProjectIngestionGetMessageHandler(final IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void validate(IngestionGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "missing ingestion id");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_GET)
    @Override
    public void handle(Session session, IngestionGenericMessage message) throws WriteResponseException {
        ingestionService.getIngestionPayload(message.getIngestionId())
            .doOnEach(logger.reactiveErrorThrowable("error fetching the ingestionRequest"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(summary -> {
                Responses.writeReactive(session,
                                        new BasicResponseMessage(PROJECT_INGESTION_GET_OK, message.getId())
                                                .addField("ingestion", summary));
            }, ex -> {
                logger.jsonDebug("Unable to fetch ingestionRequest", new HashMap<String, Object>() {
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_GET_ERROR,
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching the ingestionRequest");
            });
    }
}
