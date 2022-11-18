package com.smartsparrow.workspace.service;

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
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.workspace.subscription.ProjectEventProducer;


import reactor.core.publisher.Mono;

class ProjectBrokerTest {

    @InjectMocks
    private ProjectBroker projectBroker;

    @Mock
    private IngestionService ingestionService;

    @Mock
    private ProjectEventProducer projectEventProducer;

    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId =  UUID.randomUUID();
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
        MockitoAnnotations.openMocks(this);

        when(ingestionService.findById(ingestionId)).thenReturn(Mono.just(ingestionSummary));

        when(projectEventProducer.buildProjectEventConsumable(any(UUID.class), any(UUID.class), any(IngestionStatus.class)))
                .thenReturn(projectEventProducer);
    }

    @Test
    void broadcast() {

        final IngestionSummary summary = projectBroker.broadcast(ingestionId)
                .block();

        assertNotNull(summary);
        assertNotNull(summary.getStatus());
        assertEquals(IngestionStatus.COMPLETED, summary.getStatus());

        verify(ingestionService).findById(eq(ingestionId));
        verify(projectEventProducer).buildProjectEventConsumable(summary.getProjectId(), summary.getId(), summary.getStatus());
    }

}
