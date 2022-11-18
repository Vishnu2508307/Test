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
import com.smartsparrow.rtm.message.recv.plugin.PluginAccountSummaryMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PluginAccountSummaryMessageHandlerTest {

    @InjectMocks
    private PluginAccountSummaryMessageHandler handler;
    @Mock
    private PluginService pluginService;
    @Mock
    private AccountService accountService;
    @Mock
    private Session session;

    private static final UUID PLUGIN_ID = UUID.fromString("5cd282f2-3935-11e8-8e95-0b4f8b6658d1");
    private static final UUID ACCOUNT_ID_1 = UUID.fromString("4d9ab180-fca0-11e7-a6fc-99ccd3cd48f6");
    private static final UUID ACCOUNT_ID_2 = UUID.fromString("ced0ca90-ff17-11e7-82d0-e5862c5c616c");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noPluginId() {
        IllegalArgumentFault result =
                assertThrows(IllegalArgumentFault.class, () -> handler.validate(new PluginAccountSummaryMessage()));
        assertEquals("pluginId field is missing", result.getMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getResponseStatusCode());
    }

    @Test
    void validate_negativeLimit() {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, -3);

        IllegalArgumentFault result =
                assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("limit should be >= 0", result.getMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getResponseStatusCode());
    }

    @Test
    void validate_maxLimit() {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, Integer.MAX_VALUE + 100);

        IllegalArgumentFault result =
                assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("limit should be >= 0", result.getMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getResponseStatusCode());
    }

    @Test
    void handle_noAccounts() throws WriteResponseException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, null);

        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"workspace.plugin.account.summary.ok\",\"response\":{\"total\":0,\"collaborators\":[]}}");
    }

    @Test
    void handle_limitIsZero() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 0);

        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1)));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.account.summary.ok", response.getType());
                assertEquals(1, response.getResponse().get("total"));

                List accountList = ((List) response.getResponse().get("collaborators"));
                assertEquals(0, accountList.size());
            });
        });
    }

    @Test
    void handle_success() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 3);

        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1).setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        when(accountService.getCollaboratorPayload(ACCOUNT_ID_1, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.account.summary.ok", response.getType());
                assertEquals(1, response.getResponse().get("total"));

                List accountList = ((List) response.getResponse().get("collaborators"));
                assertEquals(1, accountList.size());
            });
        });
    }

    @Test
    void handle_totalMoreThenCount() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 1);

        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1).setPermissionLevel(PermissionLevel.CONTRIBUTOR),
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_2).setPermissionLevel(PermissionLevel.OWNER)));

        when(accountService.getCollaboratorPayload(ACCOUNT_ID_1, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));
        when(accountService.getCollaboratorPayload(ACCOUNT_ID_2, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.account.summary.ok", response.getType());
                assertEquals(2, response.getResponse().get("total"));

                List accountList = ((List) response.getResponse().get("collaborators"));
                assertEquals(1, accountList.size());
            });
        });
    }

    private PluginAccountSummaryMessage mockMessage(UUID pluginId, Integer limit) {
        PluginAccountSummaryMessage message = mock(PluginAccountSummaryMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getLimit()).thenReturn(limit);
        return message;
    }
}
