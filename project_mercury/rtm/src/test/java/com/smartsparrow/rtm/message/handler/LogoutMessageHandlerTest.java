package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.BearerToken;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.LogoutMessage;

public class LogoutMessageHandlerTest {

    @InjectMocks
    private LogoutMessageHandler logoutMessageHandler;
    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;
    @Mock
    private CredentialService credentialService;
    private MutableAuthenticationContext mutableAuthenticationContext;
    private Session session;

    private static final UUID ACCOUNT_UUID = UUID.fromString("f7313c20-15d0-11e8-b207-3da58e2e88c5");
    private static final String BEARER_TOKEN = "Zcv2sIKRKOdjtZuJpJ4nodQS63s9ju";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(ACCOUNT_UUID);
        when(mutableAuthenticationContext.getAccount()).thenReturn(account);

    }

    @Test
    void handle() throws WriteResponseException {
        LogoutMessage message = new LogoutMessage().setBearerToken(BEARER_TOKEN);
        when(credentialService.findBearerToken(BEARER_TOKEN)).thenReturn(new BearerToken().setToken(BEARER_TOKEN));
        WebSessionToken webToken = new WebSessionToken().setAccountId(ACCOUNT_UUID);
        when(credentialService.findWebSessionToken(eq(BEARER_TOKEN))).thenReturn(webToken);

        logoutMessageHandler.handle(session, message);

        verify(credentialService).invalidate(eq(webToken));
        verify(mutableAuthenticationContext).setAccount(null);
        verifySentMessage(session, "{\"type\":\"me.logout.ok\"}");
    }

    @Test
    void handle_oldToken() throws WriteResponseException {
        LogoutMessage message = new LogoutMessage().setBearerToken(BEARER_TOKEN);
        when(credentialService.findBearerToken(BEARER_TOKEN)).thenReturn(null);

        logoutMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"me.logout.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to logout: supplied bearer token is not valid\"}}");
    }

    @Test
    void handle_noToken() throws WriteResponseException {
        logoutMessageHandler.handle(session, new LogoutMessage());

        verifySentMessage(session, "{\"type\":\"me.logout.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to logout: bearerToken is required\"}}");
    }

    @Test
    void handle_tokenEmptyString() throws WriteResponseException {
        logoutMessageHandler.handle(session, new LogoutMessage().setBearerToken(""));

        verifySentMessage(session, "{\"type\":\"me.logout.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to logout: bearerToken is required\"}}");
    }


    @Test
    void handle_webTokenNotFound() throws WriteResponseException {
        LogoutMessage message = new LogoutMessage().setBearerToken(BEARER_TOKEN);
        when(credentialService.findBearerToken(BEARER_TOKEN)).thenReturn(new BearerToken().setToken(BEARER_TOKEN));
        when(credentialService.findWebSessionToken(eq(BEARER_TOKEN))).thenReturn(null);

        logoutMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"me.logout.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to logout: token 'Zcv2sIKRKOdjtZuJpJ4nodQS63s9ju' doesn't belong to the current user\"}}");
    }

    @Test
    void handle_webTokenAnotherAccount() throws WriteResponseException {
        LogoutMessage message = new LogoutMessage().setBearerToken(BEARER_TOKEN);
        when(credentialService.findBearerToken(BEARER_TOKEN)).thenReturn(new BearerToken().setToken(BEARER_TOKEN));
        WebSessionToken webToken = new WebSessionToken().setAccountId(UUID.randomUUID());
        when(credentialService.findWebSessionToken(eq(BEARER_TOKEN))).thenReturn(webToken);

        logoutMessageHandler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"me.logout.error\",\"code\":400," +
                "\"response\":{\"reason\":\"Unable to logout: token 'Zcv2sIKRKOdjtZuJpJ4nodQS63s9ju' doesn't belong to the current user\"}}");
    }
}
