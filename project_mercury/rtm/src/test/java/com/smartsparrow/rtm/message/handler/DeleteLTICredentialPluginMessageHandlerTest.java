package com.smartsparrow.rtm.message.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.LTIPluginMessage;

import reactor.test.publisher.TestPublisher;

class DeleteLTICredentialPluginMessageHandlerTest {

    @InjectMocks
    private DeleteLTICredentialPluginMessageHandler handler;

    @Mock
    private LTIPluginMessage message;

    @Mock
    private PluginService pluginService;

    private Session session;

    private static final UUID pluginId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getKey()).thenReturn("uiCerifyKey");
        when(message.getPluginId()).thenReturn(pluginId);
    }

    @Test
    void validate_noKey() {
        when(message.getKey()).thenReturn(null);
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("key is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

        when(message.getKey()).thenReturn("");

        t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("key is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("pluginId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("any error"));

        when(pluginService.deleteLTIProviderCredential(message.getKey(), message.getPluginId()))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"lti.credentials.delete.error\",\"code\":422,\"message\":\"error deleting the credentials\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();

        when(pluginService.deleteLTIProviderCredential(message.getKey(), message.getPluginId()))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"lti.credentials.delete.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
