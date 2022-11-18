package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.GetPluginInfoMessage;

import reactor.core.publisher.Mono;

class GetPluginInfoMessageHandlerTest {

    @InjectMocks
    private GetPluginInfoMessageHandler handler;

    @Mock
    private PluginService pluginService;

    private Session session;

    private static final UUID PLUGIN_ID = UUID.fromString("5cd282f2-3935-11e8-8e95-0b4f8b6658d1");
    private static final String NAME = "Breadboard";
    private static final String VERSION = "1.2.0";
    private static final UUID PUBLISHER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noPluginId() {
        GetPluginInfoMessage message = new GetPluginInfoMessage();

        RTMValidationException result = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode());
        assertEquals("workspace.plugin.get.error", result.getType());
        assertEquals("pluginId is required", result.getErrorMessage());
    }

    @Test
    void handle_versionParserException() throws WriteResponseException, VersionParserFault {
        GetPluginInfoMessage message = mock(GetPluginInfoMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);

        when(pluginService.findPluginInfo(eq(PLUGIN_ID), eq(null)))
                .thenThrow(new VersionParserFault("version can't be parsed"));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.plugin.get.error\",\"code\":400," +
                "\"message\":\"Unable to fetch plugin info for pluginId '" + PLUGIN_ID + "': version can't be parsed\"}");
    }

    @Test
    void handle_pluginNotFoundException() throws WriteResponseException, VersionParserFault {
        GetPluginInfoMessage message = mock(GetPluginInfoMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);

        when(pluginService.findPluginInfo(eq(PLUGIN_ID), eq(null)))
                .thenThrow(new PluginNotFoundFault("plugin can't be found"));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.plugin.get.error\",\"code\":404," +
                "\"message\":\"Unable to fetch plugin info for pluginId '" + PLUGIN_ID + "': plugin can't be found\"}");
    }

    @Test
    void handle_success() throws IOException, VersionParserFault {
        GetPluginInfoMessage message = mock(GetPluginInfoMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getVersion()).thenReturn(VERSION);
        PluginPayload expectedPlugin = createPlugin();
        when(pluginService.findPluginInfo(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.just(expectedPlugin));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.get.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("plugin"));
                assertEquals(PLUGIN_ID.toString(), ((Map) responseMap.get("summary")).get("pluginId"));
                assertEquals(NAME, ((Map) responseMap.get("summary")).get("name"));
                assertEquals(PLUGIN_ID.toString(), ((Map) responseMap.get("manifest")).get("id"));
                assertEquals(VERSION, ((Map) responseMap.get("manifest")).get("version"));
                assertEquals(PUBLISHER_ID.toString(), ((Map) responseMap.get("manifest")).get("publisherId"));
            });
        });
    }

    private static PluginPayload createPlugin() {
        PluginSummary summary = new PluginSummary().setId(PLUGIN_ID).setName(NAME);
        PluginManifest manifest = new PluginManifest().setPluginId(PLUGIN_ID).setVersion(VERSION).setPublisherId(PUBLISHER_ID);

        return new PluginPayload().setPluginSummaryPayload(PluginSummaryPayload.from(summary, new AccountPayload()))
                .setManifest(manifest);
    }




}
