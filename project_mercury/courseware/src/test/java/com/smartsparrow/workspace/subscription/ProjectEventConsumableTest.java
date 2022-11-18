package com.smartsparrow.workspace.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.util.UUIDs;

class ProjectEventConsumableTest {

    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();

    @Mock
    private IngestionStatus ingestionStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void projectEvent() {

        ProjectBroadcastMessage message = new ProjectBroadcastMessage()
                .setProjectId(projectId)
                .setIngestionId(ingestionId)
                .setIngestionStatus(ingestionStatus);
        ProjectEventConsumable consumable = new ProjectEventConsumable(message);

        assertEquals(new ProjectRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("workspace.project/%s", projectId),
                     consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
