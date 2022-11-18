package com.smartsparrow.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.service.WebTokenType;

import reactor.core.publisher.Mono;

class MyCloudAuthenticationServiceTest {

    @InjectMocks
    private MyCloudAuthenticationService authenticationService;

    @Mock
    private MyCloudService myCloudService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private AccountService accountService;
    @Mock
    private MyCloudCredentials credentials;

    private static final String token = "token";
    private static final String pearsonUid = "pearsonUid";
    private static final UUID accountId = UUID.randomUUID();
    private static final String email = "Support@dev";
    private static final String azureToken = "XVmSN2YflJ9ggRmaq7G6AkPYElU17dkZw9SJKJRLR4vrKxH1D8d4VZ7T058NnWiAN6fLtVpD5EeBicG4W56UtjuTBnHevLAebAokWk62Ga0AbSNepS";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(credentials.getType()).thenReturn(AuthenticationType.MYCLOUD);
        when(credentials.getToken()).thenReturn(token);
        when(myCloudService.validateToken(token)).thenReturn(Mono.just(pearsonUid));
        when(credentialService.createWebSessionToken(accountId, token))
                .thenReturn(Mono.just(new WebSessionToken()
                                              .setToken(token)
                                              .setAccountId(accountId)
                                              .setValidUntilTs(123)
                                              .setCreatedTs(321)));
    }

    @Test
    void authenticate_missingPearsonUid() {
        when(credentials.getToken()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                                              () -> authenticationService.authenticate(credentials));

        assertEquals("token is required", f.getMessage());
    }

    @Test
    void authenticate_findAccountByEmail() {
        when(myCloudService.getProfile(pearsonUid, token)).thenReturn(Mono.just(new IdentityProfile()
                                                                                        .setPrimaryEmail(email)));
        when(accountService.findAccountByEmail(email)).thenReturn(Mono.just(new Account()
                                                                                    .setId(accountId)));
        MyCloudWebSession session = authenticationService.authenticate(credentials).block();
        assertNotNull(session);
        assertEquals(AuthenticationType.MYCLOUD, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        MyCloudWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(pearsonUid, webToken.getPearsonUid());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(WebTokenType.MY_CLOUD, webToken.getWebTokenType());

        verify(myCloudService).validateToken(token);
        verify(accountService).findAccountByEmail(email);
        verify(myCloudService).getProfile(pearsonUid, token);
        verify(credentialService).createWebSessionToken(accountId, token);
    }

    @Test
    void authenticate_withBearerToken() {
        when(credentials.getToken()).thenReturn(azureToken);
        when(myCloudService.validateToken(azureToken)).thenReturn(Mono.just(pearsonUid));
        when(myCloudService.getProfile(pearsonUid, azureToken)).thenReturn(Mono.just(new IdentityProfile()
                                                                                        .setPrimaryEmail(email)));
        when(accountService.findAccountByEmail(email)).thenReturn(Mono.just(new Account()
                                                                                    .setId(accountId)));
        when(credentialService.createWebSessionToken(accountId, azureToken)).
                thenReturn(Mono.just(new WebSessionToken().setToken(token).setValidUntilTs(123)));

        MyCloudWebSession session = authenticationService.authenticate(azureToken).block();
        assertNotNull(session);
        assertEquals(AuthenticationType.MYCLOUD, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        MyCloudWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(pearsonUid, webToken.getPearsonUid());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(WebTokenType.MY_CLOUD, webToken.getWebTokenType());

        verify(myCloudService).validateToken(azureToken);
        verify(accountService).findAccountByEmail(email);
        verify(myCloudService).getProfile(pearsonUid, azureToken);
        verify(credentialService).createWebSessionToken(accountId, azureToken);
    }

}
