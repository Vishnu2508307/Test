package com.smartsparrow.rtm.diffsync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.RTMWebSocketTestUtils;

class RTMChannelFactoryTest {

    @InjectMocks
    RTMChannelFactory rtmChannelFactory;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        RTMChannel channel = rtmChannelFactory.create(session, clientId);
        assertNotNull(channel);
        assertEquals(session, channel.getSession());
        assertEquals(clientId, channel.getClientId());
    }
}