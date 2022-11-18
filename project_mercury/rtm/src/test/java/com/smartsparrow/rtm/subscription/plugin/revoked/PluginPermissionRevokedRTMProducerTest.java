package com.smartsparrow.rtm.subscription.plugin.revoked;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

public class PluginPermissionRevokedRTMProducerTest {

    @InjectMocks
    private PluginPermissionRevokedRTMProducer pluginPermissionRevokedRTMProducer;

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
    void buildConsumable() {
        pluginPermissionRevokedRTMProducer.buildPluginPermissionRevokedRTMConsumable(rtmClientContext, pluginId, accountId, teamId);
        assertNotNull(pluginPermissionRevokedRTMProducer.getEventConsumable());
    }
}
