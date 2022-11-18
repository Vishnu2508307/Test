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
import com.smartsparrow.rtm.message.recv.ingestion.ProjectIngestionListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ProjectIngestionListMessageHandler implements MessageHandler<ProjectIngestionListMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ProjectIngestionListMessageHandler.class);

    public static final String PROJECT_INGESTION_LIST = "project.ingest.list";
    public static final String PROJECT_INGESTION_LIST_OK = "project.ingest.list.ok";
    public static final String PROJECT_INGESTION_LIST_ERROR = "project.ingest.list.error";

    private final IngestionService ingestionService;

    @Inject
    public ProjectIngestionListMessageHandler(final IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void validate(ProjectIngestionListMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "missing project id");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_INGESTION_LIST)
    @Override
    public void handle(Session session, ProjectIngestionListMessage message) throws WriteResponseException {
        ingestionService.fetchIngestionsForProject(message.getProjectId())
                // filtering ingestions based on rootElementId
                .filter(i -> i.getRootElementId() == null)
                .doOnEach(logger.reactiveErrorThrowable("error listing the ingestion summaries",
                                                        throwable -> new HashMap<String, Object>() {
                                                            {
                                                                put("projectId", message.getProjectId());
                                                            }
                                                        }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(events -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(PROJECT_INGESTION_LIST_OK, message.getId())
                                                    .addField("ingestionSummaries", events));
                }, ex -> {
                    logger.jsonDebug("Unable to list ingestion summaries", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session,
                                            message.getId(),
                                            PROJECT_INGESTION_LIST_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            "error listing the ingestion summaries");
                });
    }
}
