package com.smartsparrow.rtm.subscription.plugin.granted;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription;
import com.smartsparrow.util.UUIDs;

public class PluginPermissionGrantedRTMConsumableTest {

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID pluginId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID teamId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        PluginPermissionBroadcastMessage message = new PluginPermissionBroadcastMessage(pluginId, accountId, teamId);
        PluginPermissionGrantedRTMConsumable consumable = new PluginPermissionGrantedRTMConsumable(rtmClientContext, message);

        assertEquals(rtmClientContext, consumable.getRTMClientContext());
        assertEquals(new PluginPermissionGrantedRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(PluginPermissionRTMSubscription.NAME(pluginId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
