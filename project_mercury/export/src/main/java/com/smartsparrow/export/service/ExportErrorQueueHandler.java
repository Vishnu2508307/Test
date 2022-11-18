package com.smartsparrow.export.service;

import javax.inject.Inject;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.export.data.ExportErrorNotification;

public class ExportErrorQueueHandler {

    private final ExportService exportService;
    private final ExportResultBroker exportResultBroker;

    @Inject
    public ExportErrorQueueHandler(ExportService exportService, ExportResultBroker exportResultBroker) {
        this.exportService = exportService;
        this.exportResultBroker = exportResultBroker;
    }

    /**
     * Process the incoming export error notification then broadcast an update on the export subscription
     *
     * @param notification the incoming error notification
     */
    @Handler
    @Trace(dispatcher = true, metricName = "courseware-element-to-ambrosia-result")
    public void handle(@Body ExportErrorNotification notification) {

        //
        // process the error notification
        exportService.processErrorNotification(notification)
                .block();

        // broadcast the error notification
        exportResultBroker.broadcast(notification.getExportId())
                .block();
    }

}



