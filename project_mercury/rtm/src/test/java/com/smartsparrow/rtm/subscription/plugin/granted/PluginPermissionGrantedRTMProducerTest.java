package com.smartsparrow.rtm.subscription.plugin.granted;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

public class PluginPermissionGrantedRTMProducerTest {

    @InjectMocks
    private PluginPermissionGrantedRTMProducer pluginPermissionGrantedRTMProducer;

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
        pluginPermissionGrantedRTMProducer.buildPluginPermissionGrantedRTMConsumable(rtmClientContext, pluginId, accountId, teamId);
        assertNotNull(pluginPermissionGrantedRTMProducer.getEventConsumable());
    }
}
