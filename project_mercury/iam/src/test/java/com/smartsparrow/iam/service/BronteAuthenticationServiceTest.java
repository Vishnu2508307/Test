package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Passwords;

import reactor.core.publisher.Mono;

public class BronteAuthenticationServiceTest {

    @InjectMocks
    private BronteAuthenticationService authenticationService;

    @Mock
    private AccountService accountService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private BronteCredentials credentials;

    @Mock
    private Account account;

    private static final String token = "token";
    private static final String email = "some@email.dev";
    private static final String password = "somePassword";
    private static final String validPasswordHash = Passwords.hash(password);
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(credentials.getBearerToken()).thenReturn(token);
        when(credentials.getEmail()).thenReturn(email);
        when(credentials.getPassword()).thenReturn(password);
        when(account.getId()).thenReturn(accountId);

    }

    @Test
    void authenticate_missingBearerTokenOrCredentials() {
        when(credentials.getBearerToken()).thenReturn(null);
        when(credentials.getEmail()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                                              () -> authenticationService.authenticate(credentials));

        assertEquals("either bearerToken or email and password are required", f.getMessage());
    }

    @Test
    void authenticate_withBearerToken_success() {
        when(accountService.findAccountByToken(token)).thenReturn(Mono.just(account));
        when(credentialService.findWebSessionTokenReactive(token)).thenReturn(Mono.just(new WebSessionToken()
                                                                                                .setToken(token)
                                                                                                .setValidUntilTs(123)
                                                                                                .setCreatedTs(345)));

        BronteWebSession session = authenticationService.authenticate(credentials).block();

        assertNotNull(session);
        assertEquals(AuthenticationType.BRONTE, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        BronteWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(token, webToken.getToken());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(345, webToken.getCreatedTs());
        assertEquals(WebTokenType.BRONTE, webToken.getWebTokenType());

        verify(accountService, atLeastOnce()).findAccountByToken(token);
        verify(credentialService, atLeastOnce()).findWebSessionTokenReactive(token);
        verify(accountService, never()).findAccountByEmail(email);
        verify(credentialService, never()).createWebSessionToken(accountId);
    }

    @Test
    void authenticate_withCredentials_success() {
        when(credentials.getBearerToken()).thenReturn(null);
        when(accountService.findAccountByEmail(email)).thenReturn(Mono.just(account));
        when(account.getPasswordHash()).thenReturn(validPasswordHash);
        when(credentialService.createWebSessionToken(accountId)).thenReturn(Mono.just(new WebSessionToken()
                                                                                                .setToken(token)
                                                                                                .setValidUntilTs(123)
                                                                                                .setCreatedTs(345)));

        BronteWebSession session = authenticationService.authenticate(credentials).block();

        assertNotNull(session);
        assertEquals(AuthenticationType.BRONTE, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        BronteWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(token, webToken.getToken());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(345, webToken.getCreatedTs());
        assertEquals(WebTokenType.BRONTE, webToken.getWebTokenType());

        verify(accountService, never()).findAccountByToken(token);
        verify(credentialService, never()).findWebSessionTokenReactive(token);
        verify(accountService, atLeastOnce()).findAccountByEmail(email);
        verify(credentialService, atLeastOnce()).createWebSessionToken(accountId);
    }
}
