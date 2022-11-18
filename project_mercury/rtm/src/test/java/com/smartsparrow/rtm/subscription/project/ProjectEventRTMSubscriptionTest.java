package com.smartsparrow.rtm.subscription.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class ProjectEventRTMSubscriptionTest {

    @InjectMocks
    private ProjectEventRTMSubscription subscription;

    @Mock
    private ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory;

    private static final UUID projectId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new ProjectEventRTMSubscription(projectId);
        when(projectEventRTMSubscriptionFactory.create(projectId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(projectEventRTMSubscriptionFactory.create(projectId), subscription);
        assertEquals(ProjectEventRTMSubscription.NAME(projectId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("workspace.project.broadcast", subscription.getBroadcastType());
        assertEquals(ProjectEventRTMSubscription.class, subscription.getSubscriptionType());
    }

}
