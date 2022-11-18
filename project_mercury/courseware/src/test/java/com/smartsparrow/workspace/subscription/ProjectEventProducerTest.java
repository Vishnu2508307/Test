package com.smartsparrow.workspace.subscription;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.util.UUIDs;

class ProjectEventProducerTest {

    @InjectMocks
    private ProjectEventProducer projectEventProducer;
    @Mock
    private IngestionStatus ingestionStatus;

    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        projectEventProducer.buildProjectEventConsumable(projectId,
                                                         ingestionId,
                                                         ingestionStatus);
        assertNotNull(projectEventProducer.getEventConsumable());
    }

}
