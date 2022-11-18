package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

public class ActivityIngestionProducerTest {

    @InjectMocks
    private ActivityIngestionProducer activityIngestionProducer;

    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final Object ingestionStatus = "UPLOADING";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        activityIngestionProducer.buildIngestionConsumable(ingestionId, projectId, rootElementId, ingestionStatus);
        assertNotNull(activityIngestionProducer.getEventConsumable());
    }

}
