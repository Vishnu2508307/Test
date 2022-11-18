package com.smartsparrow.rtm.diffsync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.util.UUIDs;

import data.Ack;
import data.Exchangeable;
import data.Message;

class RTMChannelTest {

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String clientId = "clientId";
    private static final UUID id = UUIDs.timeBased();
    private static final ObjectMapper om = new ObjectMapper();

    @Mock
    private Exchangeable body;

    @Mock
    private MessageTypeBridgeConverter converter;

    private Message<? extends Exchangeable> message;
    private RTMChannel rtmChannel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = Message.build(new Ack()
                .setClientId(clientId)
                .setId(id));

        when(converter.from(message.getBody().getType())).thenReturn(MessageTypeBridgeConverter.DIFF_SYNC_ACK);

        rtmChannel = new RTMChannel(session, clientId, converter);
    }

    @Test
    void createChannel() {
        assertNotNull(rtmChannel);
        assertEquals(session, rtmChannel.getSession());
        assertEquals(clientId, rtmChannel.getClientId());
    }

    @Test
    void send_converterError() {
        when(converter.from(message.getBody().getType())).thenThrow(new IllegalArgumentFault("foo"));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> rtmChannel.send(message));

        assertNotNull(f);
        assertEquals("foo", f.getMessage());
    }

    @Test
    void send() {
        rtmChannel.send(message);

        final String expected = "{" +
                                    "\"type\":\"diffSync.ack\"," +
                                    "\"response\":{" +
                                        "\"id\":\"" + id + "\"," +
                                        "\"clientId\":\"" + clientId + "\"," +
                                        "\"type\":\"ACK\"" +
                                    "}" +
                                "}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}