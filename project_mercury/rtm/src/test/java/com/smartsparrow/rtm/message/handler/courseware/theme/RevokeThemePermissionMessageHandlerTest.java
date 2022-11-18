package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.message.handler.courseware.theme.RevokeThemePermissionMessageHandler.THEME_PERMISSION_REVOKE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.RevokeThemePermissionMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class RevokeThemePermissionMessageHandlerTest {

    @InjectMocks
    private RevokeThemePermissionMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private RevokeThemePermissionMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String messageId = "message id";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getThemeId()).thenReturn(themeId);
        when(message.getId()).thenReturn(messageId);
    }

    @Test
    void validate_accountAndTeamSupplied() {
        when(message.getTeamId()).thenReturn(teamId);
        when(message.getAccountId()).thenReturn(accountId);


        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("too many arguments supplied. Either accountIds or teamIds is required", ex.getMessage());

    }

    @Test
    void validate_accountAndTeamNotSupplied() {
        when(message.getTeamId()).thenReturn(null);
        when(message.getAccountId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("either accountIds or teamIds is required", ex.getMessage());
    }

    @Test
    void validate_themeIdNotSupplied() {
        when(message.getThemeId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("themeId is required", ex.getMessage());
    }

    @Test
    void handle_success_teams() throws IOException {
        when(message.getAccountId()).thenReturn(null);
        when(message.getTeamId()).thenReturn(teamId);
        when(themeService.deleteTeamPermission(eq(teamId), eq(themeId)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(THEME_PERMISSION_REVOKE_OK, response.getType());
            });
        });
        verify(themeService, times(1))
                .deleteTeamPermission(eq(teamId), eq(themeId));
    }

    @Test
    void handle_success_accounts() throws IOException {
        when(message.getTeamId()).thenReturn(null);
        when(message.getAccountId()).thenReturn(accountId);
        when(themeService.deleteAccountPermissions(eq(accountId), eq(themeId)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(THEME_PERMISSION_REVOKE_OK, response.getType());
            });
        });
        verify(themeService, times(1))
                .deleteAccountPermissions(eq(accountId), eq(themeId));
    }

    @Test
    void handle_fail() throws IOException {

        TestPublisher<Void> error = TestPublisher.create();
        when(message.getTeamId()).thenReturn(null);
        when(message.getAccountId()).thenReturn(accountId);
        when(themeService.deleteAccountPermissions(eq(accountId), eq(themeId)))
                .thenReturn(error.flux());
        error.error(new RuntimeException());

        handler.handle(session, message);
        verify(themeService, atLeastOnce())
                .deleteAccountPermissions(eq(accountId), eq(themeId));

        String expected = "{\"type\":\"theme.permission.revoke.error\"," +
                "\"code\":422," +
                "\"message\":\"error while revoking permission over a theme\"," +
                "\"replyTo\":\"message id\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
