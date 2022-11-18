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
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionProducer;

import reactor.core.publisher.Mono;

class ActivityIngestionBrokerTest {

    @InjectMocks
    private ActivityIngestionBroker activityIngestionBroker;

    @Mock
    private IngestionService ingestionService;

    @Mock
    private ActivityIngestionProducer activityIngestionProducer;

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId = UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";

    private final IngestionSummary ingestionSummary = new IngestionSummary()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setIngestionStats(null)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setRootElementId(rootElementId)
            .setStatus(IngestionStatus.COMPLETED)
            .setConfigFields(configFields)
            .setCreatorId(creatorId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(ingestionService.findById(ingestionId)).thenReturn(Mono.just(ingestionSummary));

        when(activityIngestionProducer.buildIngestionConsumable(any(UUID.class), any(UUID.class), any(UUID.class), any(IngestionStatus.class))).thenReturn(
                activityIngestionProducer);
    }

    @Test
    void broadcast() {

        final IngestionSummary summary = activityIngestionBroker.broadcast(ingestionId)
                .block();

        assertNotNull(summary);
        assertNotNull(summary.getStatus());
        assertEquals(IngestionStatus.COMPLETED, summary.getStatus());

        verify(ingestionService).findById(eq(ingestionId));
        verify(activityIngestionProducer).buildIngestionConsumable(eq(ingestionId),eq(projectId),eq(rootElementId), eq(IngestionStatus.COMPLETED));
    }

}
