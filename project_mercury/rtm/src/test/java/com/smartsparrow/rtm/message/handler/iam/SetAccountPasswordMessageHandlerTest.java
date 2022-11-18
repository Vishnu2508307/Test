package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.iam.SetAccountPasswordMessageHandler.IAM_ACCOUNT_PASSWORD_SET_ERROR;
import static com.smartsparrow.rtm.message.handler.iam.SetAccountPasswordMessageHandler.IAM_ACCOUNT_PASSWORD_SET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.iam.SetAccountPasswordMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class SetAccountPasswordMessageHandlerTest {

    @InjectMocks
    private SetAccountPasswordMessageHandler handler;
    @Mock
    private AccountService accountService;
    private Session session;
    @Mock
    private SetAccountPasswordMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final String password = "newPassword";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = mockSession();

        when(message.getAccountId()).thenReturn(accountId);
        when(message.getPassword()).thenReturn(password);
    }

    @Test
    void validate_noAccountId() {
        when(message.getAccountId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing accountId", ex.getMessage());

    }

    @Test
    void validate_noPassword() {
        when(message.getPassword()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing password", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(accountService.setAccountPassword(accountId, password)).thenReturn(Flux.empty());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_ACCOUNT_PASSWORD_SET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't set"));
        when(accountService.setAccountPassword(accountId, password)).thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_ACCOUNT_PASSWORD_SET_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to set account password\"}");
    }
}

