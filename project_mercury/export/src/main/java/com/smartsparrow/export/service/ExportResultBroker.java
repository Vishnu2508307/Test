package com.smartsparrow.export.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.export.data.ExportGateway;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportSummaryNotification;
import com.smartsparrow.export.route.CoursewareExportRoute;

import com.smartsparrow.export.subscription.ExportProducer;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ExportResultBroker {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportResultBroker.class);

    private final ExportTrackService exportTrackService;
    private final ExportService exportService;
    private final ExportGateway exportGateway;
    private final ExportProducer exportProducer;
    private final CamelReactiveStreamsService camelReactiveStreams;
    private static final ObjectMapper mapper = new ObjectMapper();
    @Inject
    public ExportResultBroker(final ExportTrackService exportTrackService,
                              final ExportService exportService,
                              final ExportGateway exportGateway,
                              final CamelReactiveStreamsService camelReactiveStreams,
                              final ExportProducer exportProducer) {
        this.exportTrackService = exportTrackService;
        this.exportService = exportService;
        this.exportGateway = exportGateway;
        this.camelReactiveStreams = camelReactiveStreams;
        this.exportProducer = exportProducer;
    }

    /**
     * Broadcast to the export subscription when the mapping is completed and the ambrosia file is generated
     *
     * @param exportId the export id to trigger the broadcast for
     * @return a mono of export summary for the provide export id
     */
    public Mono<ExportSummary> broadcast(final UUID exportId) {
        return exportTrackService.isCompleted(exportId)
                // check if the mapping is completed
                .flatMap(isCompleted -> {
                    if (isCompleted) {
                        log.jsonInfo("Export completed, generating Ambrosia", new HashMap<String, Object>(){
                            {put("exportId", exportId);}
                        });
                        // find the export summary
                        return exportService.findById(exportId)
                                // generate the ambrosia file
                                .flatMap(exportSummary -> exportService.generateAmbrosia(exportSummary)
                                        // should any error occur while reducing then log the error
                                        // and update the export summary status to FAILED
                                        .onErrorResume(throwable -> {
                                            log.jsonError("error reducing the snippets", new HashMap<String, Object>(){
                                                {put("exportId", exportId);}
                                            }, throwable);
                                            final ExportSummary updatedSummary = new ExportSummary()
                                                    .setId(exportSummary.getId())
                                                    .setElementId(exportSummary.getElementId())
                                                    .setElementType(exportSummary.getElementType())
                                                    .setProjectId(exportSummary.getProjectId())
                                                    .setWorkspaceId(exportSummary.getWorkspaceId())
                                                    .setAccountId(exportSummary.getAccountId())
                                                    .setCompletedAt(UUIDs.timeBased())
                                                    .setExportType(exportSummary.getExportType())
                                                    .setAmbrosiaUrl(exportSummary.getAmbrosiaUrl())
                                                    .setMetadata(exportSummary.getMetadata())
                                                    .setStatus(ExportStatus.FAILED);
                                            return exportGateway.persist(updatedSummary)
                                                    .then(Mono.just(updatedSummary));
                                        })
                                        .flatMap(updatedSummary -> {
                                            // broadcast to the export subscription in any case success or failure
                                            // so the client is notified
                                            if (updatedSummary.getStatus().equals(ExportStatus.FAILED)) {
                                                produceEvent(exportSummary.getId(), ExportProgress.ERROR);
                                                //send export failure notification to epub-transform-publication-notification topic
                                                sendNotificationToSns(exportId);
                                                // return the summary
                                                return Mono.just(updatedSummary);
                                            }

                                            produceEvent(exportSummary.getId(), ExportProgress.COMPLETE);
                                            // return the summary
                                            return Mono.just(updatedSummary);
                                        }));
                    } else {
                        // find the export summary
                        return exportService.findById(exportId)
                                .flatMap(summary -> {
                                    // broadcast to the export subscription so the client is notified of current progress
                                    if (summary.getStatus().equals(ExportStatus.FAILED)) {
                                        produceEvent(summary.getId(), ExportProgress.ERROR);
                                        //send export snippet failure notification to epub-transform-publication-notification topic
                                        sendNotificationToSns(exportId);
                                        // return the summary
                                        return Mono.just(summary);
                                    }

                                    produceEvent(summary.getId(), ExportProgress.SNIPPET_COMPLETE);
                                    // return the summary
                                    return Mono.just(summary);
                                });
                    }
                });
    }


    private Flux<Void> sendNotificationToSns(final UUID exportId) {

        ExportSummaryNotification message = new ExportSummaryNotification()
                .setExportId(exportId)
                .setType("EPUB_EXPORT")
                .setStatus(ExportStatus.FAILED)
                .setMessage("10005");
        try {
            camelReactiveStreams.toStream(CoursewareExportRoute.SUBMIT_EXPORT_FAILURE,
                                          mapper.writeValueAsString(message),
                                          String.class);
        }catch (IOException e) {
            log.jsonError("Failed publish SNS message for export failure", new HashMap<String, Object>() {{
                put("exportId", exportId);
            }}, e);
        }
        return Flux.empty();
    }

    /**
     * Produces a consumable event
     *
     * @param exportId the export id
     * @param exportProgress the export progress
     */
    private void produceEvent(final UUID exportId, final ExportProgress exportProgress) {
        exportProducer.buildExportConsumable(exportId, exportProgress)
                .produce();
    }
}
