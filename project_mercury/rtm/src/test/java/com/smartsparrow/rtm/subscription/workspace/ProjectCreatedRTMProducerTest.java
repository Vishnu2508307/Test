package com.smartsparrow.rtm.subscription.workspace;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ProjectCreatedRTMProducerTest {

    @InjectMocks
    private ProjectCreatedRTMProducer projectCreatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID workspaceId = UUIDs.timeBased();
    private static final UUID projectId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        projectCreatedRTMProducer.buildProjectCreatedRTMConsumable(rtmClientContext, workspaceId, projectId);
        assertNotNull(projectCreatedRTMProducer.getEventConsumable());
    }

}
