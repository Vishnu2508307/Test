package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.rtm.message.recv.iam.MyCloudAuthorizeMessage;
import com.smartsparrow.sso.service.MyCloudCredentials;
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
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.sso.service.MyCloudWebSession;
import com.smartsparrow.sso.service.MyCloudWebToken;
import com.smartsparrow.util.DateFormat;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class MyCloudAuthorizeMessageHandlerTest {

    @InjectMocks
    private MyCloudAuthorizeMessageHandler handler;

    @Mock
    private MyCloudAuthorizeMessage message;

    @Mock
    private AuthenticationService<MyCloudCredentials, MyCloudWebSession> authenticationService;

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
        MockitoAnnotations.initMocks(this);

        when(mutableAuthenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);

        when(message.getMyCloudToken()).thenReturn(pearsonToken);

        when(authenticationService.authenticate(any(MyCloudCredentials.class)))
                .thenReturn(Mono.just(new MyCloudWebSession(account)
                                              .setMyCloudWebToken(new MyCloudWebToken("token")
                                                                      .setValidUntilTs(ttl)
                                                                      .setPearsonUid(pearsonUid))));
    }

    @Test
    void validate_noMyCloudToken() {
        when(message.getMyCloudToken()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("myCloudToken is required", f.getMessage());
    }

    @Test
    void handle_existingAccount() throws WriteResponseException {
        ArgumentCaptor<MyCloudCredentials> captor = ArgumentCaptor.forClass(MyCloudCredentials.class);

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"mycloud.authorize.ok\"," +
                "\"response\":{" +
                "\"bearerToken\":\"pearsonToken\"," +
                "\"expiry\":\""+ DateFormat.asRFC1123(ttl)+"\"" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(authenticationService).authenticate(captor.capture());

        MyCloudCredentials creds = captor.getValue();
        assertNotNull(creds);
        assertEquals(pearsonToken, creds.getToken());

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(mutableAuthenticationContext).setAuthenticationType(AuthenticationType.MYCLOUD);
        verify(mutableAuthenticationContext).setPearsonToken(pearsonToken);
        verify(mutableAuthenticationContext).setPearsonUid(pearsonUid);
        verify(mutableAuthenticationContext).setAccount(account);
        verify(mutableAuthenticationContext).setWebToken(any(WebToken.class));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<MyCloudWebSession> publisher = TestPublisher.create();
        publisher.error(new UnauthorizedFault("invalid token"));

        when(authenticationService.authenticate(any(MyCloudCredentials.class))).thenReturn(publisher.mono());
        handler.handle(session, message);

        String expected = "{\"type\":\"mycloud.authorize.error\",\"code\":401,\"message\":\"invalid token\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }
}
