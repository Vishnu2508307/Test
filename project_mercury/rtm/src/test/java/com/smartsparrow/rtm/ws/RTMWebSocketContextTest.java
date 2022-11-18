package com.smartsparrow.rtm.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RTMWebSocketContextTest {

    @Test
    void contextFrom_socket() {
        RTMWebSocketContext context = RTMWebSocketContext.from("/socket");

        assertNotNull(context);
        assertEquals(RTMWebSocketContext.SOCKET, context);
    }

    @Test
    void contextFrom_learn() {
        RTMWebSocketContext context = RTMWebSocketContext.from("/learn");

        assertNotNull(context);
        assertEquals(RTMWebSocketContext.LEARN, context);
    }

    @Test
    void contextFrom_any() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> RTMWebSocketContext.from("/any"));

        assertEquals("unknown socket path `/any`", e.getMessage());
    }
}
