package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PluginAccountListMessageHandlerTest {

    @InjectMocks
    private PluginAccountListMessageHandler handler;
    @Mock
    private PluginService pluginService;
    @Mock
    private AccountService accountService;
    @Mock
    private Session session;

    private static final UUID PLUGIN_ID = UUID.fromString("5cd282f2-3935-11e8-8e95-0b4f8b6658d1");
    private static final UUID ACCOUNT_ID = UUID.fromString("4d9ab180-fca0-11e7-a6fc-99ccd3cd48f6");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void handle_success() throws IOException {
        PluginGenericMessage message = mock(PluginGenericMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID).setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        when(accountService.getCollaboratorPayload(ACCOUNT_ID, PermissionLevel.CONTRIBUTOR)).thenReturn(
                Mono.just(new AccountCollaboratorPayload())
        );

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals("workspace.plugin.account.list.ok", response.getType());
        });
    }

    @Test
    void handle_noAccounts() throws WriteResponseException {
        PluginGenericMessage message = mock(PluginGenericMessage.class);
        when(message.getPluginId()).thenReturn(PLUGIN_ID);
        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.plugin.account.list.ok\",\"response\":{\"collaborators\":[]}}");
    }


    @Test
    void validate_noPlugin() {
        IllegalArgumentFault result =
                assertThrows(IllegalArgumentFault.class, () -> handler.validate(new PluginGenericMessage()));
        assertEquals("Unable to fetch accounts list for plugin: pluginId field is missing", result.getMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getResponseStatusCode());
    }
}
