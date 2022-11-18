package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.DeveloperKeyService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;

class DeveloperKeyProvisionMessageHandlerTest {

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private DeveloperKeyService developerKeyService;

    private DeveloperKeyProvisionMessageHandler developerKeyProvisionMessageHandler;
    private Session session;
    private AuthenticationContext authenticationContext;
    private static final UUID subscriptionId = UUID.fromString("990f1da8-cb41-11e7-abc4-cec278b6b50a");
    private static final UUID accountId = UUID.fromString("990f1da8-cb41-11e7-acc4-cec278b6b50a");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        developerKeyProvisionMessageHandler = new DeveloperKeyProvisionMessageHandler(authenticationContextProvider,
                developerKeyService);

        session = RTMWebSocketTestUtils.mockSession();
        authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
    }

    @Test
    void handle() throws WriteResponseException {
        EmptyReceivedMessage msg = mock(EmptyReceivedMessage.class);
        when(msg.getType()).thenReturn("iam.developerKey.create");
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setSubscriptionId(subscriptionId)
                .setId(accountId));

        developerKeyProvisionMessageHandler.handle(session, msg);

        verify(developerKeyService, atLeastOnce()).createKey(subscriptionId, accountId);
        verifySentMessage(session, "{\"type\":\"iam.developerKey.create.ok\"}");
    }
}
