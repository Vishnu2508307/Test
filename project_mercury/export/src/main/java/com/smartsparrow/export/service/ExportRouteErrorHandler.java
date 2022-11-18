package com.smartsparrow.export.service;

import static com.smartsparrow.export.route.CoursewareExportRoute.NOTIFICATION_ATTRIBUTE;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

/**
 * This class is responsible for handling any error that occurs to the export while processing the camel routes.
 * At this stage it could be that the lambda completed successfully but something went wrong in camel while processing.
 * The implemented {@link Handler} will make sure the error is registered in the database and the whole export is marked
 * as failed.
 */
public class ExportRouteErrorHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportRouteErrorHandler.class);

    private final ExportService exportService;
    private final ExportResultBroker exportResultBroker;

    @Inject
    public ExportRouteErrorHandler(ExportService exportService, ExportResultBroker exportResultBroker) {
        this.exportService = exportService;
        this.exportResultBroker = exportResultBroker;
    }

    @SuppressWarnings("unchecked")
    @Handler
    @Trace(dispatcher = true, metricName = "courseware-export-route-error")
    public void handle(final Exchange exchange) {
        // first read the exception and log it
        final Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        log.jsonError("an error occurred while processing the courseware export camel routes", new HashMap<>(), exception);
        // get the exchange property holding the attributes so we can extract the notificationId
        UUID notificationId = (UUID) exchange.getProperty(NOTIFICATION_ATTRIBUTE);
        if (notificationId == null) {
            log.jsonDebug("could not handle export error", new HashMap<String, Object>(){
                {put("attributes", exchange.getProperties());}
            });
            throw new IllegalStateFault("could not handle export error, notificationId attribute is missing");
        }

        // find the notification
        exportService.findNotification(notificationId)
                // create an error notification
                .flatMap(exportResultNotification -> {
                    ExportErrorNotification notificationError = new ExportErrorNotification()
                            .setErrorMessage(exception.getMessage())
                            .setCause(exception.getCause() != null ? exception.getCause().getMessage() : "unknown cause")
                            .setExportId(exportResultNotification.getExportId())
                            .setNotificationId(notificationId);
                    // save the error
                    return exportService.processErrorNotification(notificationError);
                })
                // broadcast the result to the export subscription
                .flatMap(exportResultNotification -> exportResultBroker.broadcast(exportResultNotification.getExportId()))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .block();
    }
}
