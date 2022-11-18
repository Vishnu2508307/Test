package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.message.handler.plugin.UnpublishPluginVersionMessageHandler.AUTHOR_PLUGIN_VERSION_UNPUBLISH_ERROR;
import static com.smartsparrow.rtm.message.handler.plugin.UnpublishPluginVersionMessageHandler.AUTHOR_PLUGIN_VERSION_UNPUBLISH_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.plugin.PluginVersionUnpublishMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class UnpublishPluginVersionMessageHandlerTest {

    @Mock
    private PluginService pluginService;
    @Mock
    PluginVersionUnpublishMessage message;

    private UnpublishPluginVersionMessageHandler handler;
    private Session session;

    private static final UUID pluginId = UUID.randomUUID();
    private static final Integer major = 1;
    private static final Integer minor = 22;
    private static final Integer patch = 144;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getMajor()).thenReturn(major);
        when(message.getMinor()).thenReturn(minor);
        when(message.getPatch()).thenReturn(patch);
        handler = new UnpublishPluginVersionMessageHandler(pluginService);
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("pluginId is required", ex.getMessage());
    }

    @Test
    void validate_noMajor() {
        when(message.getMajor()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("major version is required", ex.getMessage());
    }

    @Test
    void validate_noMinor() {
        when(message.getMinor()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("minor version is required", ex.getMessage());
    }

    @Test
    void validate_noPatch() {
        when(message.getPatch()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("patch version is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        UUID creatorId = UUID.randomUUID();
        PluginSummary pluginSummary = new PluginSummary()
                .setId(pluginId)
                .setLatestVersion("2.0.0")
                .setCreatorId(creatorId)
                .setDescription("Some screen plugin")
                .setName("Screen Plugin");

        when(pluginService.unPublishPluginVersion(any(), any(), any(), any()))
                .thenReturn(Mono.just(pluginSummary));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_PLUGIN_VERSION_UNPUBLISH_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("pluginSummary"));
                assertEquals(pluginId.toString(), responseMap.get("id"));
                assertEquals(creatorId.toString(), responseMap.get("creatorId"));
            });
        });

        verify(pluginService).unPublishPluginVersion(pluginId, major, minor, patch);
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<PluginSummary> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(pluginService.unPublishPluginVersion(any(), any(), any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_PLUGIN_VERSION_UNPUBLISH_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to unpublish the plugin version\"}");
    }
}
