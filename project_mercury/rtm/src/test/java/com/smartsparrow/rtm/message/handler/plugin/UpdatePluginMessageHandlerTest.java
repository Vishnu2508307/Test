package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.message.handler.plugin.UpdatePluginMessageHandler.WORKSPACE_PLUGIN_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.plugin.UpdatePluginMessageHandler.WORKSPACE_PLUGIN_UPDATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.UpdatePluginMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class UpdatePluginMessageHandlerTest {

    @Mock
    private PluginService pluginService;
    @Mock
    UpdatePluginMessage message;

    private UpdatePluginMessageHandler handler;
    private Session session;

    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPublishMode()).thenReturn(PublishMode.STRICT);
        handler = new UpdatePluginMessageHandler(pluginService);

    }

    @Test
    void validate_noElementId() {
        when(message.getPluginId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing pluginId", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getPublishMode()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing publish mode", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(pluginService.updatePluginSummary(any(), any()))
                .thenReturn(Mono.just(new PluginSummary()
                                              .setId(pluginId)
                                              .setPublishMode(PublishMode.STRICT)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_UPDATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("pluginSummary"));
                assertEquals(pluginId.toString(), responseMap.get("id"));
                assertEquals(PublishMode.STRICT.toString(), responseMap.get("publishMode"));
            });
        });

        verify(pluginService).updatePluginSummary(pluginId, PublishMode.STRICT);
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<PluginSummary> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(pluginService.updatePluginSummary(any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_UPDATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to update plugin\"}");
    }
}
