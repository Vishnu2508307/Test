package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.config.ConfigurableFeatureValues;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttribute;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.BronteCredentials;
import com.smartsparrow.iam.service.BronteWebSession;
import com.smartsparrow.iam.service.BronteWebToken;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebToken;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.AuthenticateMessage;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Maps;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AuthenticateMessageHandlerTest {

    @InjectMocks
    private AuthenticateMessageHandler authenticateMessageHandler;

    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Mock
    private AuthenticationService<BronteCredentials, BronteWebSession> bronteAuthenticationService;

    @Mock
    private AccountService accountService;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private AuthenticateMessage message;

    @Mock
    private Account account;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String email = "some@email.dev";
    private static final String token = "TpAgM0M-SLm0F8BzLSOmyf3P7-bu3byv";
    private static final String password  = "somePassword";
    private static final Long ttl = System.currentTimeMillis() + 86400000;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        account = mock(Account.class);
        when(authenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);
        when(message.getEmail()).thenReturn(email);
        when(message.getPassword()).thenReturn(password);
        when(message.getBearerToken()).thenReturn(token);
    }

    @Test
    void validate_noBronteToken() {
        when(message.getEmail()).thenReturn(null);
        when(message.getBearerToken()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticateMessageHandler.validate(message));

        assertEquals(f.getMessage(), "either bearerToken or email and password are required");

        verify(mutableAuthenticationContext, never()).setAuthenticationType(any(AuthenticationType.class));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(accountService.findShadowAttribute(account, AccountShadowAttributeName.REACTIVE_EVALUATION))
                .thenReturn(Mono.just(new AccountShadowAttribute()
                .setAttribute(AccountShadowAttributeName.REACTIVE_EVALUATION)));
        when(message.getEmail()).thenReturn(email);
        when(message.getPassword()).thenReturn(password);
        when(message.getBearerToken()).thenReturn(token);

        when(bronteAuthenticationService.authenticate(any(BronteCredentials.class)))
                .thenReturn(Mono.just(new BronteWebSession(account)
                                              .setBronteWebToken(new BronteWebToken(token)
                                                                         .setValidUntilTs(ttl))));

        ArgumentCaptor<BronteCredentials> captor = ArgumentCaptor.forClass(BronteCredentials.class);

        authenticateMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"authenticate.ok\"," +
                "\"response\":{" +
                "\"bearerToken\":\"" + token + "\"," +
                "\"expiry\":\""+ DateFormat.asRFC1123(ttl)+"\"" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(bronteAuthenticationService).authenticate(captor.capture());

        BronteCredentials cred = captor.getValue();
        assertNotNull(cred);
        assertEquals(token, cred.getBearerToken());
        assertEquals(email, cred.getEmail());
        assertEquals(password, cred.getPassword());

        verify(mutableAuthenticationContext).setAuthenticationType(AuthenticationType.BRONTE);
        verify(mutableAuthenticationContext).setAccount(account);
        verify(mutableAuthenticationContext).setWebToken(any(WebToken.class));
        verify(mutableAuthenticationContext).setConfiguredFeatures(Maps.of(ConfigurableFeatureValues.EVALUATION, AccountShadowAttributeName.REACTIVE_EVALUATION));
    }

    @Test
    void handle_error() throws WriteResponseException {
        when(message.getBearerToken()).thenReturn(null);
        TestPublisher<BronteWebSession> publisher = TestPublisher.create();
        publisher.error(new UnauthorizedFault("invalid token"));

        when(bronteAuthenticationService.authenticate(any(BronteCredentials.class))).thenReturn(publisher.mono());
        authenticateMessageHandler.handle(session, message);

        String expected = "{\"type\":\"authenticate.error\",\"code\":422,\"message\":\"authentication error\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
