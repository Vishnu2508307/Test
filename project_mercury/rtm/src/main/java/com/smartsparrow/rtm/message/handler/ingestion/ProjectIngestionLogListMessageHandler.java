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

public class ProjectIngestionLogListMessageHandler implements MessageHandler<IngestionGenericMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionLogListMessageHandler.class);

    public static final String PROJECT_INGESTION_LOG_LIST = "project.ingest.log.list";
    public static final String PROJECT_INGESTION_LOG_LIST_OK = "project.ingest.log.list.ok";
    public static final String PROJECT_INGESTION_LOG_LIST_ERROR = "project.ingest.log.list.error";

    private final IngestionService ingestionService;

    @Inject
    public ProjectIngestionLogListMessageHandler(final IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void validate(IngestionGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getIngestionId() != null, "missing ingestion id");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_LOG_LIST)
    @Override
    public void handle(Session session, IngestionGenericMessage message) throws WriteResponseException {
        ingestionService.fetchLogEventsForIngestion(message.getIngestionId())
            .doOnEach(logger.reactiveErrorThrowable("error fetching the ingestion logs"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .collectList()
            .subscribe(events -> {
                Responses.writeReactive(session,
                                        new BasicResponseMessage(PROJECT_INGESTION_LOG_LIST_OK, message.getId())
                                                .addField("ingestionEvents", events));
            }, ex -> {
                logger.jsonDebug("Unable to fetch ingestion logs", new HashMap<String, Object>() {
                    {
                        put("message", message.toString());
                        put("error", ex.getStackTrace());
                    }
                });
                Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_LOG_LIST_ERROR,
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching the ingestion logs");
            });
    }
}
