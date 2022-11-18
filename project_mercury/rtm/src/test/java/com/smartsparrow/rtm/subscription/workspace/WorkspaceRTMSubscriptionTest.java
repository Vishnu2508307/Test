package com.smartsparrow.rtm.subscription.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class WorkspaceRTMSubscriptionTest {


    @Mock
    private WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory workspaceRTMSubscriptionFactory;
    private WorkspaceRTMSubscription workspaceRTMSubscription;

    private static final UUID workspaceId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(workspaceRTMSubscriptionFactory.create(workspaceId)).thenReturn(new WorkspaceRTMSubscription(workspaceId));
        workspaceRTMSubscription = workspaceRTMSubscriptionFactory.create(workspaceId);
    }

    @Test
    void create() {
        assertEquals(workspaceRTMSubscriptionFactory.create(workspaceId), workspaceRTMSubscription);
        assertEquals(WorkspaceRTMSubscription.NAME(workspaceId), workspaceRTMSubscriptionFactory.create(workspaceId).getName());
        assertNotNull(workspaceRTMSubscription.getId());
        assertEquals("workspace.broadcast", workspaceRTMSubscription.getBroadcastType());
        assertEquals(WorkspaceRTMSubscription.class, workspaceRTMSubscription.getSubscriptionType());
    }

}