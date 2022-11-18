package com.smartsparrow.workspace.service;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.subscription.ProjectEventProducer;

import reactor.core.publisher.Mono;

public class ProjectBroker {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectBroker.class);

    private final IngestionService ingestionService;
    private final ProjectEventProducer projectEventProducer;

    @Inject
    public ProjectBroker(final IngestionService ingestionService,
                         final ProjectEventProducer projectEventProducer) {
        this.ingestionService = ingestionService;
        this.projectEventProducer = projectEventProducer;
    }

    /**
     * Broadcast to the project subscription
     *
     * @param ingestionId the export id to trigger the broadcast for
     * @return a mono of ingestion summary for the provided ingestion id
     */
    public Mono<IngestionSummary> broadcast(final UUID ingestionId) {
        // find the ingestion summary
        return ingestionService.findById(ingestionId)
                .flatMap(summary -> {
                    if (log.isDebugEnabled()) {
                        log.debug("ProjectBroker broadcast: {}", summary.toString());
                    }
                    // produce the project subscription consumable event so the client is notified of current ingestion progress
                    projectEventProducer.buildProjectEventConsumable(summary.getProjectId(), summary.getId(),
                                                                     summary.getStatus()).produce();
                    // return the summary
                    return Mono.just(summary);
                });
    }

}
