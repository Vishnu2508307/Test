package com.smartsparrow.la.service.autobahn;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import com.pearson.autobahn.common.exception.InitializationException;
import com.pearson.autobahn.common.exception.PublishException;
import com.pearson.autobahn.common.exception.SchemaNotFoundException;
import com.pearson.autobahn.common.exception.SchemaValidationException;
import com.pearson.autobahn.producersdk.AutobahnProducer;
import com.pearson.autobahn.producersdk.domain.ProducerMessage;
import com.pearson.autobahn.producersdk.impl.AutobahnProducerFactory;
import com.smartsparrow.la.config.ProducerConfig;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Singleton
public class AutobahnProducerService {

    private static final Logger log = MercuryLoggerFactory.getLogger(AutobahnProducerService.class);

    private final ProducerConfig producerConfig;

    @Inject
    public AutobahnProducerService(ProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

    public UUID produceMessage(ProducerMessage message) throws InitializationException, SchemaValidationException, SchemaNotFoundException, PublishException {
        log.info("Producing message with {}", message);
        AutobahnProducer autobahnProducer = AutobahnProducerFactory
                .getInstance(producerConfig.getOperationalType(), producerConfig.getAutobahnProducerConfig());
        UUID trackingId = autobahnProducer.publishSync(message);
        log.info("Received tracking id {}", trackingId);
        autobahnProducer.shutdown();
        return trackingId;
    }
}
