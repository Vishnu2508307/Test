package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
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

import com.smartsparrow.plugin.data.PluginVersion;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.GetPluginVersionsMessage;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Flux;

class GetPluginVersionsMessageHandlerTest {

    @InjectMocks
    private GetPluginVersionsMessageHandler getPluginVersionsMessageHandler;
    @Mock
    private PluginService pluginService;
    private Session session;

    private static final UUID PLUGIN_ID = UUID.fromString("5cd282f2-3935-11e8-8e95-0b4f8b6658d1");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void handle_noPluginId() throws WriteResponseException {
        GetPluginVersionsMessage message = new GetPluginVersionsMessage();

        getPluginVersionsMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.plugin.version.list.error\"," +
                "\"code\":400," +
                "\"response\":{\"reason\":\"Unable to fetch plugin versions: pluginId is missing\"}}");
    }

    @Test
    void handle() throws IOException {
        GetPluginVersionsMessage message = new GetPluginVersionsMessage();
        message.setPluginId(PLUGIN_ID);
        long releaseDate = System.currentTimeMillis();
        when(pluginService.getPluginVersions(eq(PLUGIN_ID))).thenReturn(
                Flux.just(createPluginVersion(2,0,0, releaseDate),
                        createPluginVersion(1,1,1, releaseDate).setPreRelease("alpha").setBuild("b234")));

        getPluginVersionsMessageHandler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.version.list.ok", response.getType());
                List responseList = ((List) response.getResponse().get("versions"));
                assertEquals(2, responseList.size());

                assertEquals("2.0.0", ((Map)responseList.get(0)).get("version"));
                assertEquals(DateFormat.asRFC1123(releaseDate), ((Map)responseList.get(0)).get("releaseDate"));

                assertEquals("1.1.1-alpha+b234", ((Map)responseList.get(1)).get("version"));
                assertEquals(DateFormat.asRFC1123(releaseDate), ((Map)responseList.get(1)).get("releaseDate"));
            });
        });
    }

    @Test
    void handle_noVersions() throws IOException {
        GetPluginVersionsMessage message = new GetPluginVersionsMessage();
        message.setPluginId(PLUGIN_ID);
        when(pluginService.getPluginVersions(eq(PLUGIN_ID))).thenReturn(Flux.empty());

        getPluginVersionsMessageHandler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.version.list.ok", response.getType());
                List responseList = ((List) response.getResponse().get("versions"));
                assertEquals(0, responseList.size());
            });
        });
    }

    private static PluginVersion createPluginVersion(int maj, int min, int patch, long releaseDate) {
        return new PluginVersion()
                .setPluginId(PLUGIN_ID)
                .setMajor(maj)
                .setMinor(min)
                .setPatch(patch)
                .setReleaseDate(releaseDate);
    }
}
