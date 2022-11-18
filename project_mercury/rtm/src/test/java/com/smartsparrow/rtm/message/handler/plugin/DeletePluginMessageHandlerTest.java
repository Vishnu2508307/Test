package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.plugin.DeletePluginMessageHandler.WORKSPACE_PLUGIN_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.plugin.DeletePluginMessageHandler.WORKSPACE_PLUGIN_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeletePluginMessageHandlerTest {

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private PluginService pluginService;

    @InjectMocks
    private DeletePluginMessageHandler handler;

    @Mock
    private PluginGenericMessage message;
    private Session session;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        AuthenticationContext context = RTMWebSocketTestUtils.mockAuthenticationContext(accountId);
        when(authenticationContextProvider.get()).thenReturn(context);

        when(message.getPluginId()).thenReturn(pluginId);
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing pluginId parameter", t.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_pluginNotExist() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("plugin doesn't exist", t.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_DELETE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate() throws RTMValidationException {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(new PluginSummary()));
        handler.validate(message);
    }

    @Test
    void handle() throws WriteResponseException {
        when(pluginService.deletePlugin(accountId, pluginId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_DELETE_OK + "\"}");
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(pluginService.deletePlugin(accountId, pluginId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_DELETE_ERROR + "\",\"code\":500," +
                "\"message\":\"Unable to delete plugin\"}");
    }
}
