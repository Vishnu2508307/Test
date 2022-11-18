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
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class WorkspaceExportListMessageHandler implements MessageHandler<WorkspaceGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceExportListMessageHandler.class);

    public static final String WORKSPACE_EXPORT_LIST = "workspace.export.list";
    public static final String WORKSPACE_EXPORT_LIST_OK = "workspace.export.list.ok";
    public static final String WORKSPACE_EXPORT_LIST_ERROR = "workspace.export.list.error";

    private final ExportService exportService;

    @Inject
    public WorkspaceExportListMessageHandler(final ExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    public void validate(WorkspaceGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getWorkspaceId() != null, "missing workspaceId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = WORKSPACE_EXPORT_LIST)
    @Override
    public void handle(Session session, WorkspaceGenericMessage message) throws WriteResponseException {
        exportService.fetchExportSummariesForWorkspace(message.getWorkspaceId())
            .doOnEach(log.reactiveErrorThrowable("error fetching exports for workspace", throwable -> new HashMap<String, Object>() {
                {
                    put("workspaceId", message.getWorkspaceId());
                }
            }))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
            .collectList()
            .subscribe(exportSummaries -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_EXPORT_LIST_OK, message.getId())
                           .addField("exportSummaries", exportSummaries));
                },
                ex -> {
                    log.debug("Unable to fetch exports for workspace", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_EXPORT_LIST_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "Unable to fetch exports for workspace");
                }
            );
    }
}
