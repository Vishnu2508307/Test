package com.smartsparrow.ingestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.subscription.IngestionProducer;

import reactor.core.publisher.Mono;

class IngestionBrokerTest {

    @InjectMocks
    private IngestionBroker ingestionBroker;

    @Mock
    private IngestionService ingestionService;

    @Mock
    private IngestionProducer ingestionProducer;

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId = UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";

    private final IngestionSummary ingestionSummary = new IngestionSummary()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setIngestionStats(null)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setStatus(IngestionStatus.COMPLETED)
            .setConfigFields(configFields)
            .setCreatorId(creatorId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(ingestionService.findById(ingestionId)).thenReturn(Mono.just(ingestionSummary));

        when(ingestionProducer.buildIngestionConsumable(any(UUID.class), any(IngestionStatus.class))).thenReturn(
                ingestionProducer);
    }

    @Test
    void broadcast() {

        final IngestionSummary summary = ingestionBroker.broadcast(ingestionId)
                .block();

        assertNotNull(summary);
        assertNotNull(summary.getStatus());
        assertEquals(IngestionStatus.COMPLETED, summary.getStatus());

        verify(ingestionService).findById(eq(ingestionId));
        verify(ingestionProducer).buildIngestionConsumable(eq(ingestionId), eq(IngestionStatus.COMPLETED));
    }

}
