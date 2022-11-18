package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.LTIProviderCredential;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.CreateLTIPluginCredentialMessage;

import reactor.core.publisher.Mono;

class CreateLTICredentialPluginMessageHandlerTest {

    @InjectMocks
    private CreateLTICredentialPluginMessageHandler handler;

    @Mock
    private CreateLTIPluginCredentialMessage message;

    @Mock
    private PluginService pluginService;

    private Session session;

    private static final UUID pluginId = UUID.randomUUID();

    private static Set<String> whiteListFields = new HashSet<>();

    @BeforeEach
    void setUp() {

        whiteListFields.add("callbackUrl");
        whiteListFields.add("userName");
        whiteListFields.add("password");
        whiteListFields.add("token");

        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getKey()).thenReturn("uiCerifyKey");
        when(message.getPluginId()).thenReturn(UUID.randomUUID());
        when(message.getSecret()).thenReturn("uiCerfitySecret");
        when(message.getWhiteListedFields()).thenReturn(whiteListFields);
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
    void validate_noSecretKey() {
        when(message.getSecret()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("secret is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

        when(message.getSecret()).thenReturn("");

        t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("secret is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }


    @Test
    void handle() throws IOException {
        LTIProviderCredential expected =
                new LTIProviderCredential()
                        .setId(UUID.randomUUID())
                        .setPluginId(pluginId)
                        .setKey("key")
                        .setSecret("secretKey")
                        .setAllowedFields(whiteListFields);

        when(pluginService.createLTIProviderCredential(anyString(), anyString(), any(), anySet())).thenReturn(
                Mono.just(expected));
        handler.handle(session, message);
        verifySentMessage(session, response -> {
            assertEquals("lti.credentials.create.ok", response.getType());
        });
    }

    @Test
    void handle_duplicateLTIError() {
        when(pluginService.createLTIProviderCredential(anyString(),
                                                       anyString(),
                                                       any(),
                                                       anySet())).thenThrow(new ConflictFault("ops"));
        ConflictFault t = assertThrows(ConflictFault.class,
                                                               () -> handler.handle(session, message));
        assertTrue(t.getMessage().startsWith("ops"));
    }
}
