package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.CredentialGateway;
import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.util.Hashing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CredentialServiceTest {

    @InjectMocks
    private CredentialService credentialService;

    @Mock
    private AccountService accountService;

    @Mock
    private CredentialGateway credentialGateway;

    //
    private UUID validAccountId = UUIDs.timeBased();
    private UUID invalidAccountId = UUIDs.timeBased();
    private String validAuthCode = UUIDs.timeBased().toString();
    private String invalidAuthCode = UUIDs.timeBased().toString();
    private CredentialTemporary.Type type = CredentialTemporary.Type.VALIDATION;
    private static final String email = "picard@starfleet.tld";
    private static final String emailAddressHash = Hashing.email(email);

    @BeforeEach
    public void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);

        // make happy cases.

        Account dbAccount = new Account()
                //
                .setId(validAccountId);

        when(accountService.verifyValidAccount(validAccountId)).thenReturn(dbAccount);
        when(accountService.verifyValidAccount(invalidAccountId)).thenThrow(new IllegalArgumentException());

        CredentialTemporary validTempCred = new CredentialTemporary() //
                .setAuthorizationCode(validAuthCode) //
                .setAccountId(validAccountId) //
                .setType(type);
        when(credentialGateway.findTemporaryByCode(validAuthCode)).thenReturn(Flux.just(validTempCred));
        when(credentialGateway.findTemporaryByAccount(validAccountId)).thenReturn(Flux.just(validTempCred));
        when(credentialGateway.findTemporaryByCode(invalidAuthCode)).thenReturn(Flux.empty());
    }

    @Test
    public void createTemporary() {
        CredentialTemporary actual = credentialService.createTemporary(validAccountId, type);

        //
        verify(accountService).verifyValidAccount(validAccountId);
        verify(credentialGateway).createBlocking(any(CredentialTemporary.class));

        assertAll("credential",
                () -> {
                    assertEquals(validAccountId, actual.getAccountId());
                    assertNotNull(actual.getAuthorizationCode());
                    assertEquals(type, actual.getType());

                });
    }

    @Test
    public void createTemporary_account_invalid() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> credentialService.createTemporary(invalidAccountId, CredentialTemporary.Type.VALIDATION));
    }

    @Test
    public void createTemporary_account_required() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> credentialService.createTemporary(null, CredentialTemporary.Type.VALIDATION));
    }

    @Test
    public void createTemporary_type_required() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> credentialService.createTemporary(validAccountId, null));
    }

    @Test
    public void verifyTemporaryCredential() {
        Account actual = credentialService.verifyTemporaryCredential(validAuthCode);

        //
        verify(accountService).verifyValidAccount(validAccountId);
        ArgumentCaptor<CredentialTemporary> captor = ArgumentCaptor.forClass(CredentialTemporary.class);
        verify(credentialGateway, atLeastOnce()).deleteBlocking(captor.capture());

        assertAll(() -> {
            assertNotNull(actual);
            assertEquals(validAuthCode, captor.getValue().getAuthorizationCode());
        });
    }

    @Test
    public void verifyTemporaryCredentials_delete_supplied_type_only() {
        CredentialTemporary cred1 = new CredentialTemporary() //
                .setAuthorizationCode(validAuthCode + "1") //
                .setAccountId(validAccountId) //
                .setType(type);
        CredentialTemporary cred2 = new CredentialTemporary() //
                .setAuthorizationCode(validAuthCode + "2") //
                .setAccountId(validAccountId) //
                .setType(CredentialTemporary.Type.PASSWORD_RESET);
        when(credentialGateway.findTemporaryByAccount(validAccountId)).thenReturn(Flux.just(cred1, cred2));

        //
        credentialService.verifyTemporaryCredential(validAuthCode);

        //
        ArgumentCaptor<CredentialTemporary> captor = ArgumentCaptor.forClass(CredentialTemporary.class);
        verify(credentialGateway, times(1)).deleteBlocking(captor.capture());
    }

    @Test
    public void verifyTemporaryCredential_code_invalid() {
        assertThrows(IllegalArgumentException.class,
                () -> credentialService.verifyTemporaryCredential(invalidAuthCode));
    }

    @Test
    public void verifyTemporaryCredential_code_required() {
        assertThrows(IllegalArgumentException.class,
                ()-> credentialService.verifyTemporaryCredential(null));
    }

    @Test
    public void createBearerToken() {
        UUID accountId = UUID.fromString("d119a97a-cb41-11e7-abc4-cec278b6b50a");
        String token = "eyJraWQiOzUxMiJ9.eyJzdWINWYyZWQyIn0.SUrghgrqFt8v-yn8b5dcBql-zgg";
        when(credentialGateway.save(any(), anyInt())).thenReturn(Flux.empty());

        WebSessionToken result = credentialService.createWebSessionToken(accountId, token).block();

        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        assertNotNull(result.getToken());

        verify(credentialGateway, times(1)).save(eq(result), anyInt());
    }

    @Test
    void findWebSessionToken() {
        String token = "Uh5MQcKRREWps_WubUaijVwgup";
        WebSessionToken expected = new WebSessionToken();
        when(credentialGateway.findWebSessionToken(token)).thenReturn(Mono.just(expected));

        WebSessionToken result = credentialService.findWebSessionToken(token);

        assertEquals(expected, result);
    }

    @Test
    void invalidateWebSessionToken() {
        WebSessionToken webSessionToken = mock(WebSessionToken.class);
        when(credentialGateway.invalidate(any())).thenReturn(Flux.empty());
        when(webSessionToken.setValidUntilTs(anyLong())).thenReturn(webSessionToken);

        credentialService.invalidate(webSessionToken);

        verify(webSessionToken).setValidUntilTs(anyLong());
        verify(credentialGateway).invalidate(eq(webSessionToken));
    }

    @Test
    void invalidateWebSessionToken_bearer_noToken() {
        assertThrows(NullPointerException.class, () -> credentialService.invalidate((BearerToken)null));
    }

    @Test
    void invalidateWebSessionToken_string_noToken() {
        assertThrows(IllegalArgumentFault.class, () -> credentialService.invalidate((String)null));
    }

    @Test
    void fetchCredential_success(){
        when(credentialGateway.fetchCredentialTypeByHash(any())).thenReturn(Flux.just(new CredentialsType()
        .setAuthenticationType(AuthenticationType.MYCLOUD)
        .setAccountId(UUID.randomUUID())
        .setHash(emailAddressHash)));
        CredentialsType credentialTypeByHash = credentialService.fetchCredentialTypeByHash(email).blockFirst();
        assertNotNull(credentialTypeByHash);
        verify(credentialGateway).fetchCredentialTypeByHash(emailAddressHash);

    }

}
