package com.smartsparrow.rtm.subscription.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ProjectCreatedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID workspaceId = UUIDs.timeBased();
    private static final UUID projectId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        ProjectCreatedBroadcastMessage message = new ProjectCreatedBroadcastMessage(workspaceId, projectId);
        ProjectCreatedRTMConsumable consumable = new ProjectCreatedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new ProjectCreatedRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(WorkspaceRTMSubscription.NAME(workspaceId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}