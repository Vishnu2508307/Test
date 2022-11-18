package com.smartsparrow.export.service;

import javax.inject.Inject;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ExportResultQueueHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportResultQueueHandler.class);

    private final ExportService exportService;
    private final ExportResultBroker exportResultBroker;

    @Inject
    public ExportResultQueueHandler(ExportService exportService, ExportResultBroker exportResultBroker) {
        this.exportService = exportService;
        this.exportResultBroker = exportResultBroker;
    }

    /**
     * Create an {@link ExportAmbrosiaSnippet} from the incoming export result notification then broadcast the update
     * on the export subscription
     *
     * @param notification the incoming notification to process
     */
    @Handler
    @Trace(dispatcher = true, metricName = "courseware-element-to-ambrosia-result")
    public void handle(@Body ExportResultNotification notification) {
        // create the export ambrosia snippet to save
        final ExportAmbrosiaSnippet snippet = exportService.create(notification.getExportId(),
                notification.getNotificationId(),
                notification.getElementId(),
                notification.getElementType(),
                notification.getAccountId(),
                notification.getAmbrosiaSnippet()).block();

        // process the snippet result
        exportService.processResultSnippet(snippet)
                .block();

        // broadcast an update on the export subscription
        exportResultBroker.broadcast(notification.getExportId())
                .block();
    }

}



