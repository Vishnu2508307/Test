package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.plugin.PluginCollaboratorSummaryMessageHandler.WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK;
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

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.PluginAccountSummaryMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PluginCollaboratorSummaryMessageHandlerTest {

    @InjectMocks
    private PluginCollaboratorSummaryMessageHandler handler;
    @Mock
    private PluginService pluginService;
    @Mock
    private AccountService accountService;
    @Mock
    private TeamService teamService;
    @Mock
    private Session session;

    private static final UUID PLUGIN_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID_1 = UUID.randomUUID();
    private static final UUID ACCOUNT_ID_2 = UUID.randomUUID();
    private static final UUID TEAM_ID_1 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void validate_noPluginId() {
        RTMValidationException result =
                assertThrows(RTMValidationException.class, () -> handler.validate(new PluginAccountSummaryMessage()));

        assertEquals("pluginId field is missing", result.getErrorMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void validate_negativeLimit() {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, -3);

        RTMValidationException result =
                assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("limit '-3' should be >= 0", result.getErrorMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void validate_maxLimit() {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, Integer.MAX_VALUE + 100);

        RTMValidationException result =
                assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("limit '-2147483549' should be >= 0", result.getErrorMessage());
        assertEquals(HttpStatus.SC_BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void handle_noCollaborators() throws WriteResponseException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, null);

        when(pluginService.findAccountCollaborators(PLUGIN_ID)).thenReturn(Flux.empty());
        when(pluginService.findTeamCollaborators(PLUGIN_ID)).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK + "\"," +
                "\"response\":{\"total\":0,\"collaborators\":{}}}");
    }

    @Test
    void handle_limitIsZero() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 0);

        when(pluginService.findTeamCollaborators(PLUGIN_ID)).thenReturn(Flux.empty());
        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1)));
        when(accountService.getCollaboratorPayload(ACCOUNT_ID_1, null))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK, response.getType());
                assertEquals(1, response.getResponse().get("total"));

                Map accountList = ((Map) response.getResponse().get("collaborators"));
                assertEquals(0, accountList.size());
            });
        });
    }

    @Test
    void handle_success() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 3);

        when(pluginService.findTeamCollaborators(PLUGIN_ID)).thenReturn(Flux.just(
                new PluginTeamCollaborator().setTeamId(TEAM_ID_1).setPermissionLevel(PermissionLevel.REVIEWER)
        ));
        when(teamService.getTeamCollaboratorPayload(TEAM_ID_1, PermissionLevel.REVIEWER))
                .thenReturn(Mono.just(TeamCollaboratorPayload.from(new TeamSummary().setId(TEAM_ID_1), PermissionLevel.REVIEWER)));

        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1).setPermissionLevel(PermissionLevel.CONTRIBUTOR)));
        when(accountService.getCollaboratorPayload(ACCOUNT_ID_1, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(AccountCollaboratorPayload.from(new AccountPayload().setAccountId(ACCOUNT_ID_1), PermissionLevel.CONTRIBUTOR)));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(
                    () -> assertEquals(WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK, response.getType()),
                    () -> assertEquals(2, response.getResponse().get("total")),
                    () -> {
                        Map collaborators = ((Map) response.getResponse().get("collaborators"));
                        List teams = (List) collaborators.get("teams");
                        assertEquals(1, teams.size());
                        Map team = (Map) teams.get(0);
                        assertEquals("REVIEWER", team.get("permissionLevel"));
                        assertEquals(TEAM_ID_1.toString(), ((Map) team.get("team")).get("id"));

                        List accounts = (List) collaborators.get("accounts");
                        Map account = (Map) accounts.get(0);
                        assertEquals("CONTRIBUTOR", account.get("permissionLevel"));
                        assertEquals(ACCOUNT_ID_1.toString(), ((Map) account.get("account")).get("accountId"));
                    });
        });
    }

    @Test
    void handle_totalMoreThenCount() throws IOException {
        PluginAccountSummaryMessage message = mockMessage(PLUGIN_ID, 1);

        when(pluginService.findTeamCollaborators(PLUGIN_ID)).thenReturn(Flux.empty());
        when(pluginService.findAccountCollaborators(eq(PLUGIN_ID))).thenReturn(Flux.just(
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_1).setPermissionLevel(PermissionLevel.CONTRIBUTOR),
                new PluginAccountCollaborator().setAccountId(ACCOUNT_ID_2).setPermissionLevel(PermissionLevel.OWNER)));

        when(accountService.getCollaboratorPayload(ACCOUNT_ID_1, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));
        when(accountService.getCollaboratorPayload(ACCOUNT_ID_2, PermissionLevel.OWNER))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_PLUGIN_COLLABORATOR_SUMMARY_OK, response.getType());
                assertEquals(2, response.getResponse().get("total"));

                Map accountList = ((Map) response.getResponse().get("collaborators"));
                assertEquals(1, ((List) accountList.get("accounts")).size());
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
