package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ProjectExportListMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectExportListMessageHandler.class);

    public static final String PROJECT_EXPORT_LIST = "project.export.list";
    public static final String PROJECT_EXPORT_LIST_OK = "project.export.list.ok";
    public static final String PROJECT_EXPORT_LIST_ERROR = "project.export.list.error";

    private final ExportService exportService;

    @Inject
    public ProjectExportListMessageHandler(final ExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    public void validate(ProjectGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "missing projectId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_EXPORT_LIST)
    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {
        exportService.fetchExportSummariesForProject(message.getProjectId())
            .doOnEach(log.reactiveErrorThrowable("error fetching exports for project", throwable -> new HashMap<String, Object>() {
                {
                    put("projectId", message.getProjectId());
                }
            }))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
            .collectList()
            .subscribe(exportSummaries -> {
                    Responses.writeReactive(session, new BasicResponseMessage(PROJECT_EXPORT_LIST_OK, message.getId())
                           .addField("exportSummaries", exportSummaries));
                },
                ex -> {
                    log.jsonDebug("Unable to fetch exports for project", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), PROJECT_EXPORT_LIST_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to fetch exports for project");
                }
            );
    }
}
