package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.sso.service.IESService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AccountIdentityServiceTest {

    @InjectMocks
    private AccountIdentityService accountInformationService;

    @Mock
    private IESService iesService;

    @Mock
    private AccountService accountService;

    @Mock
    private CredentialService credentialService;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testFetchAccountSummaryPayload_BRONTE() {
        UUID accountId = UUID.randomUUID();
        when(accountService.getAccountSummaryPayload(any(UUID.class))).thenReturn(Mono.just(new AccountSummaryPayload().setAccountId(
                accountId)
                                                                                                    .setAvatarSmall(
                                                                                                            "avatarSmall")
                                                                                                    .setPrimaryEmail(
                                                                                                            "email@")));
        when(credentialService.fetchCredentialTypeByAccount(any())).thenReturn(Flux.just(new CredentialsType()
                                                                                                 .setAccountId(accountId)
                                                                                                 .setAuthenticationType(
                                                                                                         AuthenticationType.BRONTE)
                                                                                                 .setHash("hash1")));
        List<AccountSummaryPayload> accountSummaryPayloads = accountInformationService.fetchAccountSummaryPayload(
                Arrays.asList(accountId)).collectList().block();
        assertNotNull(accountSummaryPayloads);
        verify(accountService).getAccountSummaryPayload(any(UUID.class));
        verify(credentialService).fetchCredentialTypeByAccount(accountId);
    }

    @Test
    void testFetchAccountSummaryPayload_IES() {
        UUID accountId = UUID.randomUUID();
        when(credentialService.fetchCredentialTypeByAccount(any())).thenReturn(Flux.just(new CredentialsType()
                                                                                                 .setAccountId(accountId)
                                                                                                 .setAuthenticationType(
                                                                                                         AuthenticationType.IES)
                                                                                                 .setHash("hash1")));
        when(iesService.findIESId(any(UUID.class))).thenReturn(Mono.just(new IESAccountTracking().setAccountId(
                accountId)
                                                                                 .setIesUserId(
                                                                                         "iesUser1")));
        when(iesService.getAccountSummaryPayload(any(List.class))).thenReturn(Flux.just(new AccountSummaryPayload().setAccountId(
                accountId)
                                                                                                .setAvatarSmall(
                                                                                                        "avatarSmall")
                                                                                                .setPrimaryEmail(
                                                                                                        "email@")));

        List<AccountSummaryPayload> accountSummaryPayloads = accountInformationService.fetchAccountSummaryPayload(
                Arrays.asList(accountId)).collectList().block();
        assertNotNull(accountSummaryPayloads);
        verify(credentialService).fetchCredentialTypeByAccount(accountId);
        verify(iesService).findIESId(accountId);
        verify(iesService).getAccountSummaryPayload(any(List.class));
    }

    @Test
    void testFetchAccountSummaryPayload_LTI() {
        UUID accountId = UUID.randomUUID();
        when(credentialService.fetchCredentialTypeByAccount(any())).thenReturn(Flux.just(new CredentialsType()
                                                                                                 .setAccountId(accountId)
                                                                                                 .setAuthenticationType(
                                                                                                         AuthenticationType.LTI)
                                                                                                 .setHash("hash1")));
        when(iesService.findIESId(any(UUID.class))).thenReturn(Mono.just(new IESAccountTracking().setAccountId(
                accountId)
                                                                                 .setIesUserId(
                                                                                         "iesUser1")));
        when(iesService.getAccountSummaryPayload(any(List.class))).thenReturn(Flux.just(new AccountSummaryPayload().setAccountId(
                accountId)
                                                                                                .setAvatarSmall(
                                                                                                        "avatarSmall")
                                                                                                .setPrimaryEmail(
                                                                                                        "email@")));

        List<AccountSummaryPayload> accountSummaryPayloads = accountInformationService.fetchAccountSummaryPayload(
                Arrays.asList(accountId)).collectList().block();
        assertNotNull(accountSummaryPayloads);
        verify(credentialService).fetchCredentialTypeByAccount(accountId);
        verify(iesService).findIESId(accountId);
        verify(iesService).getAccountSummaryPayload(any(List.class));
    }

}
