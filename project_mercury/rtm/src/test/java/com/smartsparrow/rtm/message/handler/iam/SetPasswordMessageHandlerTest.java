package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.iam.SetPasswordMessageHandler.IAM_PASSWORD_SET_ERROR;
import static com.smartsparrow.rtm.message.handler.iam.SetPasswordMessageHandler.IAM_PASSWORD_SET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.iam.SetPasswordMessage;
import com.smartsparrow.util.Passwords;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class SetPasswordMessageHandlerTest {

    @InjectMocks
    private SetPasswordMessageHandler handler;
    @Mock
    private AccountService accountService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    private Session session;
    @Mock
    private SetPasswordMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final String clearTextPassword = "Alpha-Alpha-3-0-5";
    private static final String passwordHash = Passwords.hash(clearTextPassword);
    private static final String password = "newPassword";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId).setPasswordHash(passwordHash));

        when(message.getOldPassword()).thenReturn(clearTextPassword);
        when(message.getNewPassword()).thenReturn(password);
        when(message.getConfirmNew()).thenReturn(password);
        when(accountService.verifyPassword(clearTextPassword, passwordHash)).thenReturn(true);
    }

    @Test
    void validate_noOldPassword() {
        when(message.getOldPassword()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing oldPassword", ex.getMessage());

    }

    @Test
    void validate_noNewPassword() {
        when(message.getNewPassword()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing newPassword", ex.getMessage());
    }

    @Test
    void validate_noConfirmNew() {
        when(message.getConfirmNew()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing confirmNew", ex.getMessage());
    }

    @Test
    void validate_OldPasswordMismatch() {
        when(message.getOldPassword()).thenReturn(password);
        when(accountService.verifyPassword(password, passwordHash)).thenReturn(false);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("provided password doesn't match existing password", ex.getMessage());
    }

    @Test
    void validate_NewPasswordMismatch() {
        when(message.getNewPassword()).thenReturn(clearTextPassword);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("new password does not match confirm new password", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(accountService.setAccountPassword(accountId, password)).thenReturn(Flux.empty());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_PASSWORD_SET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't set"));
        when(accountService.setAccountPassword(accountId, password)).thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_PASSWORD_SET_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to set password\"}");
    }
}

