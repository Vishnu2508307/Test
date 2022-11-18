package com.smartsparrow.rtm.subscription.plugin;

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

public class PluginPermissionRTMSubscriptionTest {

    @InjectMocks
    private PluginPermissionRTMSubscription subscription;

    @Mock
    private PluginPermissionRTMSubscription.PluginPermissionRTMSubscriptionFactory pluginPermissionRTMSubscriptionFactory;

    private static final UUID pluginId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new PluginPermissionRTMSubscription(pluginId);
        when(pluginPermissionRTMSubscriptionFactory.create(pluginId)).thenReturn(subscription);

    }

    @Test
    void create() {
        assertEquals(pluginPermissionRTMSubscriptionFactory.create(pluginId), subscription);
        assertEquals(PluginPermissionRTMSubscription.NAME(pluginId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("workspace.plugin.permission.broadcast", subscription.getBroadcastType());
        assertEquals(PluginPermissionRTMSubscription.class, subscription.getSubscriptionType());
    }
}
