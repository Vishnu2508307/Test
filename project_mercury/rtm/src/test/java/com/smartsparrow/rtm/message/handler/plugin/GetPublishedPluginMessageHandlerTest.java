package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.plugin.data.ManifestView;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.plugin.GetPublishedPluginMessage;

import reactor.core.publisher.Mono;

class GetPublishedPluginMessageHandlerTest {

    @InjectMocks
    private GetPublishedPluginMessageHandler getPluginMessageHandler;
    @Mock
    private PluginService pluginService;

    private Session session;

    private static final UUID PLUGIN_ID = UUID.fromString("5cd282f2-3935-11e8-8e95-0b4f8b6658d1");
    private static final String VIEW = "EDITOR";
    private static final String NAME = "Breadboard";
    private static final String VERSION = "1.2.0";
    private static final UUID PUBLISHER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noPlugin() {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        Throwable t = assertThrows(RTMValidationException.class, ()-> getPluginMessageHandler.validate(message));
        assertEquals("pluginId is required", ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void handle_noView() throws IOException, VersionParserFault {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getVersion()).thenReturn(VERSION);

        PluginPayload expected = createPlugin();

        when(pluginService.findPlugin(PLUGIN_ID, VERSION)).thenReturn(Mono.just(expected));

        getPluginMessageHandler.handle(session, message);

        verifyPluginReturned();
    }

    @Test
    void handle_noVersion() throws IOException, VersionParserFault {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getView()).thenReturn(VIEW);

        PluginPayload expected = createPlugin();

        when(pluginService.findPluginByIdAndView(eq(PLUGIN_ID), eq(VIEW), eq(null)))
                .thenReturn(Mono.just(expected));

        getPluginMessageHandler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("author.plugin.get.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("plugin"));
                assertEquals(3, responseMap.size());
            });
        });
    }

    @Test
    void handle() throws IOException, VersionParserFault {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getView()).thenReturn(VIEW);
        when(message.getVersion()).thenReturn(VERSION);

        PluginPayload expected = createPlugin();

        when(pluginService.findPluginByIdAndView(eq(PLUGIN_ID), eq(VIEW), eq(VERSION)))
                .thenReturn(Mono.just(expected));

        getPluginMessageHandler.handle(session, message);

        verifyPluginReturned();
    }

    private void verifyPluginReturned() throws IOException {
        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("author.plugin.get.ok", response.getType());
                Map responseMap = ((Map) response.getResponse().get("plugin"));
                assertEquals(PLUGIN_ID.toString(), ((Map) responseMap.get("summary")).get("pluginId"));
                assertEquals(NAME, ((Map) responseMap.get("summary")).get("name"));
                assertEquals(PLUGIN_ID.toString(), ((Map) responseMap.get("manifest")).get("id"));
                assertEquals(VERSION, ((Map) responseMap.get("manifest")).get("version"));
                assertEquals(PUBLISHER_ID.toString(), ((Map) responseMap.get("manifest")).get("publisherId"));
                assertEquals(PLUGIN_ID.toString(), ((Map) ((List) responseMap.get("entryPoints")).get(0)).get("pluginId"));
                assertEquals(VERSION, ((Map) ((List) responseMap.get("entryPoints")).get(0)).get("version"));
                assertEquals(VIEW, ((Map) ((List) responseMap.get("entryPoints")).get(0)).get("context"));
            });
        });
    }

    @Test
    void handle_pluginNotFound() throws IOException, VersionParserFault {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getView()).thenReturn(VIEW);
        when(message.getVersion()).thenReturn(VERSION);

        when(pluginService.findPluginByIdAndView(eq(PLUGIN_ID), eq(VIEW), eq(VERSION)))
                .thenThrow(PluginNotFoundFault.class);

        getPluginMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"author.plugin.get.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to fetch plugin: null\"}}");
    }

    @Test
    void handle_invalidVersion()  throws IOException, VersionParserFault {
        GetPublishedPluginMessage message = mock(GetPublishedPluginMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(message.getView()).thenReturn(VIEW);
        when(message.getVersion()).thenReturn(VERSION);

        when(pluginService.findPluginByIdAndView(eq(PLUGIN_ID), eq(VIEW), eq(VERSION)))
                .thenThrow(VersionParserFault.class);

        getPluginMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"author.plugin.get.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to fetch plugin: null\"}}");
    }

    private static PluginPayload createPlugin() {
        PluginSummary summary = new PluginSummary().setId(PLUGIN_ID).setName(NAME);
        PluginManifest manifest = new PluginManifest().setPluginId(PLUGIN_ID).setVersion(VERSION).setPublisherId(PUBLISHER_ID);
        ManifestView view = new ManifestView().setPluginId(PLUGIN_ID).setVersion(VERSION).setContext(VIEW);

        return new PluginPayload().setPluginSummaryPayload(PluginSummaryPayload.from(summary, new AccountPayload()))
                .setManifest(manifest).addEntryPoints(view);
    }
}
