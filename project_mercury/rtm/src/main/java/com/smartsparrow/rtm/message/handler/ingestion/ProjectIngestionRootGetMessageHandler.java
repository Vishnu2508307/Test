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
import com.smartsparrow.rtm.message.recv.ingestion.IngestionRootGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ProjectIngestionRootGetMessageHandler implements MessageHandler<IngestionRootGenericMessage> {

    public static final String PROJECT_INGESTION_ROOT_GET = "project.ingestion.root.get";
    public static final String PROJECT_INGESTION_ROOT_GET_OK = "project.ingestion.root.get.ok";
    public static final String PROJECT_INGESTION_ROOT_GET_ERROR = "project.ingestion.root.get.error";

    private final IngestionService ingestionService;

    @Inject
    public ProjectIngestionRootGetMessageHandler(final IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }
    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionRootGetMessageHandler.class);

    @Override
    public void validate(IngestionRootGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getRootElementId() != null, "missing root element id");
        affirmArgument(message.getProjectId() != null, "missing project id");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_ROOT_GET)
    @Override
    public void handle(final Session session, final IngestionRootGenericMessage message) throws WriteResponseException {
        {
            ingestionService.fetchIngestionForProjectByRootElement(message.getRootElementId())
                    .doOnEach(logger.reactiveErrorThrowable("error fetching the ingestion request by root element id"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList()
                    .subscribe(summary -> {
                        Responses.writeReactive(session,
                                                new BasicResponseMessage(PROJECT_INGESTION_ROOT_GET_OK, message.getId())
                                                        .addField("ingestionSummaries", summary));
                    }, ex -> {
                        logger.jsonDebug("Unable to fetch ingestionRequest by root element", new HashMap<String, Object>() {
                            {
                                put("message", message.toString());
                                put("error", ex.getStackTrace());
                            }
                        });
                        Responses.errorReactive(session, message.getId(), PROJECT_INGESTION_ROOT_GET_ERROR,
                                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching the ingestionRequest by root element");
                    });
        }
    }
}
