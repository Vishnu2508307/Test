package com.smartsparrow.ingestion.service;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.subscription.IngestionProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class IngestionBroker {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IngestionBroker.class);

    private final IngestionService ingestionService;
    private final IngestionProducer ingestionProducer;

    @Inject
    public IngestionBroker(final IngestionService ingestionService,
                           final IngestionProducer ingestionProducer) {
        this.ingestionService = ingestionService;
        this.ingestionProducer = ingestionProducer;
    }

    /**
     * Broadcast to the ingestion subscription
     *
     * @param ingestionId the export id to trigger the broadcast for
     * @return a mono of ingestion summary for the provide ingestion id
     */
    public Mono<IngestionSummary> broadcast(final UUID ingestionId) {
        // find the ingestion summary
        return ingestionService.findById(ingestionId)
                .flatMap(summary -> {
                    if (log.isDebugEnabled()) {
                        log.debug("IngestionBroker broadcast: {}", summary.toString());
                    }
                    // broadcast to the ingestion subscription so the client is notified of current progress
                    ingestionProducer.buildIngestionConsumable(summary.getId(),
                                                               summary.getStatus()).produce();
                    // return the summary
                    return Mono.just(summary);
                });
    }
}
