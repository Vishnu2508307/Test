package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.IESAuthorizeMessage;
import com.smartsparrow.sso.service.IESCredentials;
import com.smartsparrow.sso.service.IESWebSession;
import com.smartsparrow.sso.service.IESWebToken;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class IESAuthorizeMessageHandlerTest {

    @InjectMocks
    private IESAuthorizeMessageHandler handler;

    @Mock
    private IESAuthorizeMessage message;

    @Mock
    private AuthenticationService<IESCredentials, IESWebSession> authenticationService;

    @Mock
    private Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private static final String pearsonToken = "pearsonToken";
    private static final String pearsonUid = "pearsonUid";
    private static final UUID accountId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final Long ttl = System.currentTimeMillis() + 86400000;
    private static final Account account = new Account()
            .setId(accountId);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mutableAuthenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);

        when(message.getPearsonToken()).thenReturn(pearsonToken);
        when(message.getPearsonUid()).thenReturn(pearsonUid);

        when(authenticationService.authenticate(any(IESCredentials.class)))
                .thenReturn(Mono.just(new IESWebSession(account)
                        .setIesWebToken(new IESWebToken("token")
                                .setValidUntilTs(ttl)
                                .setPearsonUid(pearsonUid))));
    }

    @Test
    void validate_noPearsonUid() {
        when(message.getPearsonUid()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("pearsonUid is required", f.getMessage());
    }

    @Test
    void validate_noPearsonToken() {
        when(message.getPearsonToken()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("pearsonToken is required", f.getMessage());
    }

    @Test
    void handle_newAccount() throws WriteResponseException {
        ArgumentCaptor<IESCredentials> captor = ArgumentCaptor.forClass(IESCredentials.class);

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"ies.authorize.ok\"," +
                            "\"response\":{" +
                                "\"bearerToken\":\"pearsonToken\"," +
                                "\"expiry\":\""+ DateFormat.asRFC1123(ttl)+"\"" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(authenticationService).authenticate(captor.capture());

        IESCredentials creds = captor.getValue();
        assertNotNull(creds);
        assertEquals(pearsonUid, creds.getPearsonUid());
        assertEquals(pearsonToken, creds.getToken());
        assertNull(creds.getInvalidBearerToken());

        verify(mutableAuthenticationContext).setAuthenticationType(AuthenticationType.IES);
        verify(mutableAuthenticationContext).setPearsonToken(pearsonToken);
        verify(mutableAuthenticationContext).setPearsonUid(pearsonUid);
        verify(mutableAuthenticationContext).setAccount(account);
        verify(mutableAuthenticationContext).setWebToken(any(WebToken.class));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<IESWebSession> publisher = TestPublisher.create();
        publisher.error(new UnauthorizedFault("invalid token"));

        when(authenticationService.authenticate(any(IESCredentials.class))).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"ies.authorize.error\",\"code\":422,\"message\":\"ies authorization error\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }
}
