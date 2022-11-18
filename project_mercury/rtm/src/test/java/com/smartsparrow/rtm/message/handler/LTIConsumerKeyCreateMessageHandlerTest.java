package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.sso.service.LTIConsumerKey;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.sso.service.LTIv11Service;

class LTIConsumerKeyCreateMessageHandlerTest {

    @Mock
    private LTIv11Service ltIv11Service;
    private Session session;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @InjectMocks
    private LTIConsumerKeyCreateMessageHandler ltiConsumerKeyCreateMessageHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void handle_success() throws IOException {
        AuthenticationContext authContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        when(authContext.getAccount()).thenReturn(account);
        UUID subscriptionId = UUID.randomUUID();
        when(account.getSubscriptionId()).thenReturn(subscriptionId);
        when(authenticationContextProvider.get()).thenReturn(authContext);

        when(ltIv11Service.createLTIConsumerKey(eq(subscriptionId), eq((String)null))).thenReturn(
                new LTIConsumerKey().setKey("test").setSecret("secret").setSubscriptionId(subscriptionId)
        );

        ltiConsumerKeyCreateMessageHandler.handle(session, new EmptyReceivedMessage());

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("iam.ltiConsumerKey.create.ok", response.getType());
            Map responseMap = ((Map) response.getResponse().get("ltiConsumerKey"));
            assertEquals(3, responseMap.size());
            assertEquals("test", responseMap.get("key"));
            assertEquals("secret", responseMap.get("secret"));
            assertEquals(subscriptionId.toString(), responseMap.get("subscriptionId"));
        }));
    }
}
