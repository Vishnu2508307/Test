package com.smartsparrow.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.service.WebTokenType;

import reactor.core.publisher.Mono;

class IESAuthenticationServiceTest {

    @InjectMocks
    private IESAuthenticationService authenticationService;

    @Mock
    private IESService iesService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private IESCredentials credentials;

    private static final String token = "token";
    private static final String pearsonUid = "ffffffff5f20e09d1d4b7401dfccbfb2";
    private static final String invalidBearer = "invalidBearer";
    private static final UUID accountId = UUID.randomUUID();
    private static final String jwt = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjV" +
            "mMjBlMDlkMWQ0Yjc0MDFkZmNjYmZiMiIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3MTI0NjQxLCJpYXQiOjE1OTcxMjI4ND" +
            "EsImNsaWVudF9pZCI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNzY1ZmVjZDAtMWU5ZC00ZDkxLWEzN" +
            "DktMDI2NzRkNzRkNGI4In0.Bd89NMcbydGhzv_QkP-rCXUqNNrPhl9qwXQ0czz_cKgsI66Bqi9aAcaTGeSz2awbFlOzDfrYm9fkwrlbq0ya" +
            "eowjoSVw6BAhXct_vqv83_agcY3w5fhJmpl-gUL4wj3uZIg8uKHXBF8fhjaNLdIO9HmahwAocSpH71EtLOD62nnGv3EmsF9Hzw0abpPGMSF" +
            "9gDUqRS3rjXzrAkjRzX9CX1A_odYPkP65UYSgHNfVKP7jjJHS1x-v2um6GpX435RO-F38LPRy336mEeoGoZv6X9q6i5JA5H2dauhLna728q" +
            "3Fmg2kKsLlvGi148JM4wNb6-DzxBnq_LyES0e7Iwkg1w";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(credentials.getType()).thenReturn(AuthenticationType.IES);
        when(credentials.getPearsonUid()).thenReturn(pearsonUid);
        when(credentials.getToken()).thenReturn(token);
        when(credentials.getInvalidBearerToken()).thenReturn(invalidBearer);

        when(iesService.validateToken(any())).thenReturn(Mono.just(true));

        when(credentialService.createWebSessionToken(any(), any()))
                .thenReturn(Mono.just(new WebSessionToken()
                        .setToken(token)
                        .setAccountId(accountId)
                        .setValidUntilTs(123)
                        .setCreatedTs(321)));
    }

    @Test
    void authenticate_missingPearsonToken() {
        when(credentials.getToken()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> authenticationService.authenticate(credentials));

        assertEquals("token is required", f.getMessage());
    }

    @Test
    void authenticate_missingPearsonUid() {
        when(credentials.getToken()).thenReturn(null);
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> authenticationService.authenticate(credentials));

        assertEquals("token is required", f.getMessage());
    }

    @Test
    void authenticate_foundById() {
        when(iesService.findAccount(pearsonUid))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)));

        IESWebSession session = authenticationService.authenticate(credentials)
                .block();

        assertNotNull(session);
        assertEquals(AuthenticationType.IES, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        IESWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(pearsonUid, webToken.getPearsonUid());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(WebTokenType.IES, webToken.getWebTokenType());

        verify(iesService).validateToken(token);
        verify(iesService).findAccount(pearsonUid);
        verify(iesService, never()).provisionAccount(pearsonUid);
        verify(credentialService).createWebSessionToken(accountId, token);
    }

    @Test
    void authenticate_accountProvisioned() {
        when(iesService.findAccount(pearsonUid))
                .thenReturn(Mono.empty());

        when(iesService.provisionAccount(pearsonUid))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)));

        IESWebSession session = authenticationService.authenticate(credentials)
                .block();

        assertNotNull(session);
        assertEquals(AuthenticationType.IES, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        IESWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertEquals(pearsonUid, webToken.getPearsonUid());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(WebTokenType.IES, webToken.getWebTokenType());

        verify(iesService).validateToken(token);
        verify(iesService).findAccount(pearsonUid);
        verify(iesService).provisionAccount(pearsonUid);
        verify(credentialService).createWebSessionToken(accountId, token);
    }

    @Test
    void authenticate_withJWT() {
        when(credentials.getToken()).thenReturn(jwt);
        when(iesService.findAccount(any())).thenReturn(Mono.just(new Account().setId(accountId)));

        when(iesService.provisionAccount(any())).thenReturn(Mono.just(new Account().setId(accountId)));

        IESWebSession session = authenticationService.authenticate(jwt).block();

        assertNotNull(session);
        assertEquals(AuthenticationType.IES, session.getAuthenticationType());
        assertNotNull(session.getWebToken());
        IESWebToken webToken = session.getWebToken();
        assertNotNull(webToken);
        assertNotNull(webToken.getPearsonUid());
        assertEquals(123, webToken.getValidUntilTs());
        assertEquals(WebTokenType.IES, webToken.getWebTokenType());

        verify(iesService).validateToken(jwt);
        verify(iesService).findAccount(pearsonUid);
        verify(credentialService).createWebSessionToken(accountId, jwt);
    }

}
