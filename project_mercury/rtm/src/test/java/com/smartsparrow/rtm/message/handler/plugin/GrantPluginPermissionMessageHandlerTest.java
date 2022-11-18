package com.smartsparrow.rtm.message.handler.plugin;


import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.plugin.GrantPluginPermissionMessageHandler.WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR;
import static com.smartsparrow.rtm.message.handler.plugin.GrantPluginPermissionMessageHandler.WORKSPACE_PLUGIN_GRANT_PERMISSION_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginPermissionPersistenceException;
import com.smartsparrow.plugin.service.PluginPermissionService;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.plugin.PluginPermissionMessage;
import com.smartsparrow.rtm.subscription.plugin.granted.PluginPermissionGrantedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GrantPluginPermissionMessageHandlerTest {

    @Mock
    private PluginService pluginService;
    @Mock
    private AccountService accountService;
    @Mock
    private TeamService teamService;
    @Mock
    private PluginPermissionService pluginPermissionService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private PluginPermissionGrantedRTMProducer pluginPermissionGrantedRTMProducer;

    private GrantPluginPermissionMessageHandler handler;
    private PluginPermissionMessage validAccountGrantMessage;
    private PluginPermissionMessage validTeamGrantMessage;
    private PluginPermissionMessage invalidNoAccountNoTeam;
    private PluginPermissionMessage invalidBothAccountAndTeam;
    private PluginPermissionMessage invalidNoPlugin;
    private PluginPermissionMessage invalidNoPermission;

    private Session session;
    private UUID accountId = UUID.randomUUID();
    private UUID teamId = UUID.randomUUID();
    private UUID pluginId = UUID.randomUUID();
    private PermissionLevel permissionLevel = PermissionLevel.CONTRIBUTOR;
    private static final PluginType type = PluginType.COMPONENT;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        handler = new GrantPluginPermissionMessageHandler(pluginService, accountService, teamService, pluginPermissionService,
                rtmClientContextProvider, pluginPermissionGrantedRTMProducer);
        session = RTMWebSocketTestUtils.mockSession();

        validAccountGrantMessage = mockMessage(accountId, null, pluginId, permissionLevel);
        validTeamGrantMessage = mockMessage(null, teamId, pluginId, permissionLevel);
        invalidNoAccountNoTeam = mockMessage(null, null, pluginId, permissionLevel);
        invalidBothAccountAndTeam = mockMessage(accountId, teamId, pluginId, permissionLevel);
        invalidNoPlugin = mockMessage(accountId, null, null, permissionLevel);
        invalidNoPermission = mockMessage(accountId, null, pluginId, null);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(accountService.findById(accountId)).thenReturn(Flux.just(account));

        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getId()).thenReturn(pluginId);
        when(pluginSummary.getType()).thenReturn(type);
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.just(pluginSummary));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");

    }

    @Test
    void validate_noAccountNoTeamIds() {
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(invalidNoAccountNoTeam));

        assertEquals("either accountId or teamId is required", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_bothAccountAndTeamIds() {
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(invalidBothAccountAndTeam));

        assertEquals("either accountId or teamId is required", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_successAccountId() throws RTMValidationException {
        handler.validate(validAccountGrantMessage);

        verify(pluginService, atLeastOnce()).fetchById(pluginId);
        verify(accountService, atLeastOnce()).findById(accountId);
    }

    @Test
    void validate_successTeamId() throws RTMValidationException {
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));

        handler.validate(validTeamGrantMessage);

        verify(pluginService, atLeastOnce()).fetchById(pluginId);
    }

    @Test
    void validate_noPluginId() {
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(invalidNoPlugin));

        assertEquals("pluginId is required", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, (int) e.getStatusCode());
    }

    @Test
    void validate_noPermissionLevel() {
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(invalidNoPermission));

        assertEquals("permissionLevel is required", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_accountNotFound() {
        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(validAccountGrantMessage));

        assertTrue(e.getErrorMessage().contains("not found"));
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_teamNotFound() {
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(validTeamGrantMessage));

        assertEquals("team " + teamId + " not found", e.getErrorMessage());
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void validate_pluginNotFound() {
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(validAccountGrantMessage));

        assertTrue(e.getErrorMessage().contains("not found"));
        assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR, e.getType());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
    }

    @Test
    void handle_accountGrantSuccess() throws IOException {
        when(pluginPermissionService.saveAccountPermission(accountId, pluginId, permissionLevel)).thenReturn(Flux.empty());
        when(pluginPermissionGrantedRTMProducer.buildPluginPermissionGrantedRTMConsumable(rtmClientContext,
                                                                                          pluginId,
                                                                                          accountId,
                                                                                          null))
                .thenReturn(pluginPermissionGrantedRTMProducer);

        handler.handle(session, validAccountGrantMessage);

        verifySentMessage(session, message -> {
            assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_OK, message.getType());
            assertNull(message.getResponse().get("teamId"));
            assertEquals(accountId.toString(), message.getResponse().get("accountId"));
            assertEquals(pluginId.toString(), message.getResponse().get("pluginId"));
            assertEquals(permissionLevel.toString(), message.getResponse().get("permissionLevel"));
        });
        verify(pluginPermissionGrantedRTMProducer).buildPluginPermissionGrantedRTMConsumable(eq(rtmClientContext),
                                                                                             eq(pluginId),
                                                                                             eq(accountId),
                                                                                             eq(null));
        verify(pluginPermissionGrantedRTMProducer).produce();
    }

    @Test
    void handle_teamGrantSuccess() throws IOException {
        when(pluginPermissionService.saveTeamPermission(teamId, pluginId, permissionLevel)).thenReturn(Flux.empty());
        when(pluginPermissionGrantedRTMProducer.buildPluginPermissionGrantedRTMConsumable(rtmClientContext,
                                                                                          pluginId,
                                                                                          null,
                                                                                          teamId))
                .thenReturn(pluginPermissionGrantedRTMProducer);

        handler.handle(session, validTeamGrantMessage);

        verifySentMessage(session, message -> {
            assertEquals(WORKSPACE_PLUGIN_GRANT_PERMISSION_OK, message.getType());
            assertNull(message.getResponse().get("accountId"));
            assertEquals(teamId.toString(), message.getResponse().get("teamId"));
            assertEquals(pluginId.toString(), message.getResponse().get("pluginId"));
            assertEquals(permissionLevel.toString(), message.getResponse().get("permissionLevel"));
        });
        verify(pluginPermissionGrantedRTMProducer).buildPluginPermissionGrantedRTMConsumable(eq(rtmClientContext),
                                                                                             eq(pluginId),
                                                                                             eq(null),
                                                                                             eq(teamId));
        verify(pluginPermissionGrantedRTMProducer).produce();
    }

    @Test
    void handle_failsToSaveAccountPermissions() throws WriteResponseException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new PluginPermissionPersistenceException("error"));
        when(pluginPermissionService.saveAccountPermission(accountId, pluginId, permissionLevel)).thenReturn(error.flux());

        handler.handle(session, validAccountGrantMessage);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR + "\",\"code\":422," +
                "\"message\":\"error\"}");
    }

    @Test
    void handle_failsToSaveTeamPermissions() throws WriteResponseException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new PluginPermissionPersistenceException("error"));
        when(pluginPermissionService.saveTeamPermission(teamId, pluginId, permissionLevel)).thenReturn(error.flux());

        handler.handle(session, validTeamGrantMessage);

        verifySentMessage(session, "{\"type\":\"" + WORKSPACE_PLUGIN_GRANT_PERMISSION_ERROR + "\",\"code\":422," +
                "\"message\":\"error\"}");
    }

    private PluginPermissionMessage mockMessage(UUID accountId, UUID teamId, UUID pluginId, PermissionLevel permissionLevel) {
        PluginPermissionMessage message = mock(PluginPermissionMessage.class);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getPermissionLevel()).thenReturn(permissionLevel);
        return message;
    }
}
