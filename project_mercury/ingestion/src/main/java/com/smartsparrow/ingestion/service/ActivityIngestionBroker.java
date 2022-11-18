package com.smartsparrow.ingestion.service;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class ActivityIngestionBroker {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityIngestionBroker.class);

    private final IngestionService ingestionService;
    private final ActivityIngestionProducer activityIngestionProducer;

    @Inject
    public ActivityIngestionBroker(final IngestionService ingestionService,
                                   final ActivityIngestionProducer activityIngestionProducer) {
        this.ingestionService = ingestionService;
        this.activityIngestionProducer = activityIngestionProducer;
    }

    /**
     * Broadcast to the Activity ingestion subscription
     *
     * @param ingestionId the export id to trigger the broadcast for
     * @return a mono of ingestion summary for to provide ingestion id
     */
    public Mono<IngestionSummary> broadcast(final UUID ingestionId) {
        // find the ingestion summary
        return ingestionService.findById(ingestionId)
                .flatMap(summary -> {
                    if (log.isDebugEnabled()) {
                        log.debug("ActivityIngestionBroker broadcast: {}", summary.toString());
                    }
                    // broadcast to the ingestion subscription so the client is notified of current progress
                    activityIngestionProducer.buildIngestionConsumable(summary.getId(), summary.getProjectId(),summary.getRootElementId(),
                                                                       summary.getStatus()).produce();
                    // return the summary
                    return Mono.just(summary);
                });
    }
}
