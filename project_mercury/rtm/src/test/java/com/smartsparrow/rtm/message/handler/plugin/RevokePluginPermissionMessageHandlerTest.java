package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.plugin.RevokePluginPermissionMessageHandler.WORKSPACE_PLUGIN_PERMISSION_REVOKE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginPermissionPersistenceException;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.plugin.RevokePluginPermissionMessage;
import com.smartsparrow.rtm.subscription.plugin.revoked.PluginPermissionRevokedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RevokePluginPermissionMessageHandlerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    @Mock
    private PluginService pluginService;

    @Mock
    private PluginPermissionService pluginPermissionService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private PluginPermissionRevokedRTMProducer pluginPermissionRevokedRTMProducer;

    @InjectMocks
    private RevokePluginPermissionMessageHandler handler;

    private RevokePluginPermissionMessage message;
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final PluginType type = PluginType.COMPONENT;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(RevokePluginPermissionMessage.class);
        Account targetAccount = mock(Account.class);
        PluginSummary pluginSummary = mock(PluginSummary.class);
        session = RTMWebSocketTestUtils.mockSession();

        when(pluginSummary.getType()).thenReturn(type);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getPluginId()).thenReturn(pluginId);
        when(accountService.findById(message.getAccountId())).thenReturn(Flux.just(targetAccount));
        when(pluginService.fetchById(message.getPluginId())).thenReturn(Mono.just(pluginSummary));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");

    }

    @Test
    void validate_noAccountIdAndTeamId() {
        when(message.getAccountId()).thenReturn(null);
        when(message.getTeamId()).thenReturn(null);

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> handler.validate(message));

        assertEquals("either accountId or teamId is required", t.getErrorMessage());
    }

    @Test
    void validate_bothAccountIdAndTeamId() {

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> handler.validate(message));

        assertEquals("either accountId or teamId is required", t.getErrorMessage());
    }

    @Test
    void validate_onlyAccountId() throws RTMValidationException {
        when(message.getTeamId()).thenReturn(null);

        handler.validate(message);
    }

    @Test
    void validate_onlyTeamId() throws RTMValidationException {
        when(message.getAccountId()).thenReturn(null);
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));

        handler.validate(message);
    }

    @Test
    void validate_noPluginId() {
        when(message.getAccountId()).thenReturn(null);
        when(message.getPluginId()).thenReturn(null);

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> handler.validate(message));

        assertEquals("pluginId is required", t.getErrorMessage());
    }

    @Test
    void validate_pluginNotFound() {
        when(message.getTeamId()).thenReturn(null);
        when(pluginService.fetchById(message.getPluginId())).thenReturn(Mono.empty());

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> handler.validate(message));

        assertEquals(String.format("plugin %s not found", pluginId), t.getErrorMessage());
    }

    @Test
    void validate_accountNotFound() {
        when(message.getTeamId()).thenReturn(null);
        when(accountService.findById(message.getAccountId())).thenReturn(Flux.empty());

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> handler.validate(message));

        assertEquals(String.format("account %s not found", accountId), t.getErrorMessage());
    }

    @Test
    void validate_teamNotFound() {
        when(message.getAccountId()).thenReturn(null);
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("team " + teamId + " not found", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_PERMISSION_REVOKE_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void handle_account() throws WriteResponseException {
        when(message.getTeamId()).thenReturn(null);
        when(pluginPermissionRevokedRTMProducer.buildPluginPermissionRevokedRTMConsumable(rtmClientContext,
                                                                                          pluginId,
                                                                                          accountId,
                                                                                          null))
                .thenReturn(pluginPermissionRevokedRTMProducer);
        when(pluginPermissionService.deleteAccountPermission(accountId, pluginId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.plugin.permission.revoke.ok\"}";
        verifySentMessage(session, expected);
        verify(pluginPermissionService, times(1)).deleteAccountPermission(accountId, pluginId);
        verify(pluginPermissionRevokedRTMProducer).buildPluginPermissionRevokedRTMConsumable(eq(rtmClientContext),
                                                                                             eq(pluginId),
                                                                                             eq(accountId),
                                                                                             eq(null));
        verify(pluginPermissionRevokedRTMProducer).produce();
    }

    @Test
    void handle_team() throws WriteResponseException {
        when(message.getAccountId()).thenReturn(null);
        when(pluginPermissionRevokedRTMProducer.buildPluginPermissionRevokedRTMConsumable(rtmClientContext,
                                                                                          pluginId,
                                                                                          null,
                                                                                          teamId))
                .thenReturn(pluginPermissionRevokedRTMProducer);
        when(pluginPermissionService.deleteTeamPermission(teamId, pluginId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.plugin.permission.revoke.ok\"}";
        verifySentMessage(session, expected);
        verify(pluginPermissionService, times(1)).deleteTeamPermission(teamId, pluginId);
        verify(pluginPermissionRevokedRTMProducer).buildPluginPermissionRevokedRTMConsumable(eq(rtmClientContext),
                                                                                             eq(pluginId),
                                                                                             eq(null),
                                                                                             eq(teamId));
        verify(pluginPermissionRevokedRTMProducer).produce();
    }

    @Test
    void handle_failsToDelete_account() throws WriteResponseException {
        when(message.getTeamId()).thenReturn(null);
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new PluginPermissionPersistenceException("error"));
        when(pluginPermissionService.deleteAccountPermission(accountId, pluginId)).thenReturn(error.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.plugin.permission.revoke.error\",\"code\":422,\"message\":\"error\"}";
        verifySentMessage(session, expected);

        verify(pluginPermissionService, atLeastOnce()).deleteAccountPermission(accountId, pluginId);
    }

    @Test
    void handle_failsToDelete_team() throws WriteResponseException {
        when(message.getAccountId()).thenReturn(null);
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new PluginPermissionPersistenceException("error"));
        when(pluginPermissionService.deleteTeamPermission(teamId, pluginId)).thenReturn(error.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.plugin.permission.revoke.error\",\"code\":422,\"message\":\"error\"}";
        verifySentMessage(session, expected);
        verify(pluginPermissionService, atLeastOnce()).deleteTeamPermission(teamId, pluginId);
    }
}
