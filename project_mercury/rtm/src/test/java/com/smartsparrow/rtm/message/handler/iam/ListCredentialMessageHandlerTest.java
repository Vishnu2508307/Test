package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.iam.ListCredentialMessageHandler.IAM_CREDENTIAL_LIST_OK;
import static com.smartsparrow.rtm.message.handler.iam.ListCredentialMessageHandler.IAM_CREDENTIAL_LIST_ERROR;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.iam.CredentialMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ListCredentialMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListCredentialMessageHandler handler;

    @Mock
    CredentialService credentialService;

    @Mock
    private CredentialMessage message;

    private static final String email = "random@email";
    private static final UUID accountId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getEmail()).thenReturn(email);

        session = mockSession();
    }

    @Test
    void validate_noEmail() {
        when(message.getEmail()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing email", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(credentialService.fetchCredentialTypeByHash(any()))
                .thenReturn(Flux.just(new CredentialsType()
                .setAccountId(accountId)
                .setAuthenticationType(AuthenticationType.MYCLOUD)
                .setHash("emailHash")));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(IAM_CREDENTIAL_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("credentialTypes"));
                assertEquals(accountId.toString(), ((LinkedHashMap)responseList.get(0)).get("accountId"));
                assertEquals(AuthenticationType.MYCLOUD.toString(), ((LinkedHashMap)responseList.get(0)).get("authenticationType"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CredentialsType> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(credentialService.fetchCredentialTypeByHash(any()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + IAM_CREDENTIAL_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch credential types\"}");
    }
}
