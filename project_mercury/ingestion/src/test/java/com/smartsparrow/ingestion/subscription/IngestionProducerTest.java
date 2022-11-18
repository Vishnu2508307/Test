package com.smartsparrow.ingestion.subscription;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.util.UUIDs;

class IngestionProducerTest {

    @InjectMocks
    private IngestionProducer ingestionProducer;

    private static final UUID ingestionId = UUIDs.timeBased();
    @Mock
    private IngestionStatus ingestionStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        ingestionProducer.buildIngestionConsumable(ingestionId, ingestionStatus);
        assertNotNull(ingestionProducer.getEventConsumable());
    }

}
