package com.smartsparrow.sso.service;

import static com.smartsparrow.dataevent.RouteUri.IES_BATCH_PROFILE_GET;
import static com.smartsparrow.dataevent.RouteUri.IES_TOKEN_VALIDATE;
import static com.smartsparrow.sso.service.IESService.IES_EMAIL_FORMAT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.data.IesAccountTrackingGateway;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.sso.event.IESBatchProfileGetEventMessage;
import com.smartsparrow.sso.event.IESBatchProfileGetParams;
import com.smartsparrow.sso.event.IESTokenValidationEventMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class IESServiceTest {

    @InjectMocks
    private IESService iesService;

    @Mock
    private AccountService accountService;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private IesAccountTrackingGateway iesAccountTrackingGateway;

    @Mock
    private CacheService cacheService;

    private final static String token = "eyJraWQiOiJrOTgwMy4xNTk5ODQyODg3IiwiYWxnIjoiUlM1MTIifQ.eyJzdWIiOiJmZmZmZmZmZjVmMDRlNTRlYmRjNDEyMDEzMGMyZWU0MyIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwibGFuZyI6ImVuX1VTIiwiZXhwIjoxNjI5MTI4OTgzLCJkZXZpY2VpZCI6IjU1OTlkZDEzMTdmZDNmYzQxMTk3NjQ0NCIsImlhdCI6MTYyOTEyNzE4MywiY2xpZW50X2lkIjoib1g1Vm02U0VFZVNpNVFCRUFVMDA4MXR4MjBVSEU0NjkiLCJzZXNzaWQiOiIxNWU5NTA4Yy1kYWM2LTRhZWEtODlkNi1iMTA3YWIzMjYyZDkifQ.fyMLoUIh69qGll5Dv4m4ftazw-Pq1SSJhuxVzo49gn7-xZ2nAZjCiEMBjb8A_RUb42GTiXOqx5lSX-CxdKEEcZ1RZnKO-FyUA1TeZhw5pBmo1hPus6pxIEP2cE0E2-QpJYoPR3Ck54vxdvTPoJWJ3PEkXMmt3qbKmv4KYx5MMQRbSyES5pxoC808voNKjeUSIKXzvfHXSRgDEDBcpNVplAla-3AHd3FGZJ8v6znyusSkuDrCRar8Y9ONOQhWroKwQB4izCIvcLpxpYXkHeyt6fQzvoOlHEULcnve2m6v0LCBBsPYVpWtnM0iYcopSHSKnzEkgUXvJuN-qeYhIktOqA";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateToken_nullToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> iesService.validateToken(null));

        assertNotNull(f);
        assertEquals("token is required", f.getMessage());
    }

    @Test
    void validateToken_emptyToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> iesService.validateToken(""));

        assertNotNull(f);
        assertEquals("token is required", f.getMessage());
    }

    @Test
    void validateToken_invalid() {
        Mono<IESTokenValidationEventMessage> reply = Mono.just(new IESTokenValidationEventMessage(token));
        when(camel.toStream(eq(IES_TOKEN_VALIDATE), any(IESTokenValidationEventMessage.class), eq(IESTokenValidationEventMessage.class)))
                .thenReturn(reply);
        when(cacheService.computeIfAbsent(any(), any(), any(), anyLong(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        UnauthorizedFault f = assertThrows(UnauthorizedFault.class, () -> iesService.validateToken(token).block());

        assertNotNull(f);
        assertEquals("Invalid token supplied", f.getMessage());

        ArgumentCaptor<IESTokenValidationEventMessage> captor = ArgumentCaptor
                .forClass(IESTokenValidationEventMessage.class);

        verify(camel).toStream(eq(IES_TOKEN_VALIDATE), captor.capture(), eq(IESTokenValidationEventMessage.class));
        verify(cacheService).computeIfAbsent(any(), any(), any(), anyLong(),
                any(TimeUnit.class));

        IESTokenValidationEventMessage result = captor.getValue();

        assertNotNull(result);
        assertFalse(result.getValid());
    }

    @Test
    void validateToken_failToPareToken() {

        UnauthorizedFault f = assertThrows(UnauthorizedFault.class, () ->
                iesService.validateToken("Not a token!").block());

        assertNotNull(f);
        assertEquals("Invalid token supplied", f.getMessage());

        ArgumentCaptor<IESTokenValidationEventMessage> captor = ArgumentCaptor
                .forClass(IESTokenValidationEventMessage.class);

        verify(camel, never()).toStream(eq(IES_TOKEN_VALIDATE), captor.capture(), eq(IESTokenValidationEventMessage.class));
        verify(cacheService, never()).computeIfAbsent(any(), any(), any(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    void validateToken_valid() {
        // mock the route validating the token
        IESTokenValidationEventMessage eventMessage = new IESTokenValidationEventMessage(token);
        eventMessage.markValid();
        when(cacheService.computeIfAbsent(any(), any(), any(), anyLong(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        when(camel.toStream(eq(IES_TOKEN_VALIDATE), any(IESTokenValidationEventMessage.class), eq(IESTokenValidationEventMessage.class)))
                .thenReturn(Mono.just(eventMessage));

        Boolean isValid = iesService.validateToken(token).block();

        assertNotNull(isValid);
        assertTrue(isValid);

        ArgumentCaptor<IESTokenValidationEventMessage> captor = ArgumentCaptor
                .forClass(IESTokenValidationEventMessage.class);

        verify(camel).toStream(eq(IES_TOKEN_VALIDATE), captor.capture(), eq(IESTokenValidationEventMessage.class));
        verify(cacheService).computeIfAbsent(any(), any(), any(), anyLong(),
                any(TimeUnit.class));

        IESTokenValidationEventMessage result = captor.getValue();

        assertNotNull(result);
        // token was false before reaching the route
        assertFalse(result.getValid());
    }

    @Test
    void findAccount_notFound() {
        final String pearsonUid = "pearsonUid";

        when(iesAccountTrackingGateway.findAccountId(pearsonUid)).thenReturn(Mono.empty());

        assertNull(iesService.findAccount(pearsonUid).block());

        verify(iesAccountTrackingGateway).findAccountId(pearsonUid);
        verify(accountService, never()).findById(any(UUID.class));
    }

    @Test
    void findAccount_found() {
        final String pearsonUid = "pearsonUid";
        final UUID accountId = UUID.randomUUID();
        when(iesAccountTrackingGateway.findAccountId(pearsonUid))
                .thenReturn(Mono.just(new IESAccountTracking()
                        .setIesUserId(pearsonUid)
                        .setAccountId(accountId)));

        when(accountService.findById(accountId))
                .thenReturn(Flux.just(new Account()));

        assertNotNull(iesService.findAccount(pearsonUid).block());

        verify(iesAccountTrackingGateway).findAccountId(pearsonUid);
        verify(accountService).findById(accountId);
    }

    @Test
    void provisionAccount_emptyPearsonUid() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> iesService.provisionAccount("")
                .block());

        assertNotNull(f);
        assertEquals("pearsonUid is required", f.getMessage());
    }

    @Test
    void provisionAccount_conflictException() {
        final String pearsonUid =" pearsonUid";
        doThrow(new ConflictException("conflict"))
                .when(accountService).provision(AccountProvisionSource.OIDC, null, null,
                                                null, null, String.format(IES_EMAIL_FORMAT, pearsonUid), null,
                                                null, null, false, AuthenticationType.IES);

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> iesService.provisionAccount(pearsonUid));

        assertNotNull(f);
        assertEquals("conflict", f.getMessage());
    }

    @Test
    void provisionAccount() {
        final String pearsonUid = "pearsonUid";
        final UUID accountId = UUID.randomUUID();
        final UUID subscriptionId = UUID.randomUUID();

        when(accountService.provision(AccountProvisionSource.OIDC, null, null,
                null, null, String.format(IES_EMAIL_FORMAT, pearsonUid), null,
                null, null, false, AuthenticationType.IES)).thenReturn(new AccountAdapter()
                .setAccount(new Account()
                        .setSubscriptionId(subscriptionId)
                        .setId(accountId)));

        when(subscriptionPermissionService.saveAccountPermission(accountId, subscriptionId, PermissionLevel.OWNER))
                .thenReturn(Flux.just(new Void[]{}));

        when(iesAccountTrackingGateway.persist(any(IESAccountTracking.class)))
                .thenReturn(Flux.just(new Void[]{}));

        Account account = iesService.provisionAccount(pearsonUid)
                .block();

        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals(subscriptionId, account.getSubscriptionId());

        ArgumentCaptor<IESAccountTracking> captor = ArgumentCaptor.forClass(IESAccountTracking.class);

        verify(subscriptionPermissionService).saveAccountPermission(accountId, subscriptionId, PermissionLevel.OWNER);
        verify(iesAccountTrackingGateway).persist(captor.capture());

        IESAccountTracking iesAccountTracking = captor.getValue();

        assertNotNull(iesAccountTracking);
        assertEquals(pearsonUid, iesAccountTracking.getIesUserId());
        assertEquals(accountId, iesAccountTracking.getAccountId());
    }

    @Test
    void getProfiles_nullIds() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> iesService.getProfiles(null)
                .collectList()
                .block());

        assertEquals("ids is required", f.getMessage());
    }

    @Test
    void getProfiles_emptyIds() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> iesService.getProfiles(new ArrayList<>())
                .collectList()
                .block());

        assertEquals("ids must not be empty", f.getMessage());
    }

    @Test
    void getProfiles() {
        ArgumentCaptor<IESBatchProfileGetEventMessage> messageCaptor = ArgumentCaptor.forClass(IESBatchProfileGetEventMessage.class);

        final List<String> ids = Lists.newArrayList("idOne", "idTwo");
        final IdentityProfile identityOne = new IdentityProfile()
                .setFamilyName("Morgan")
                .setGivenName("Arthur")
                .setPrimaryEmail("reddeadredemption@dev.dev")
                .setId("idOne");

        IESBatchProfileGetEventMessage message = new IESBatchProfileGetEventMessage(new IESBatchProfileGetParams(ids));
        message.setIdentityProfile(Lists.newArrayList(identityOne));
        message.setNotFound(Lists.newArrayList("idTwo"));
        when(camel.toStream(eq(IES_BATCH_PROFILE_GET), any(IESBatchProfileGetEventMessage.class), eq(IESBatchProfileGetEventMessage.class)))
                .thenReturn(Mono.just(message));

        List<IdentityProfile> profiles = iesService.getProfiles(ids)
                .collectList()
                .block();

        verify(camel).toStream(eq(IES_BATCH_PROFILE_GET), messageCaptor.capture(), eq(IESBatchProfileGetEventMessage.class));

        IESBatchProfileGetEventMessage capturedMessage = messageCaptor.getValue();

        assertAll(() -> {
            assertNotNull(profiles);
            assertEquals(2, profiles.size());
            assertNotNull(capturedMessage);
            assertEquals(2, capturedMessage.getParams().getPiIds().size());

            IdentityProfile found = profiles.get(0);
            IdentityProfile notFound = profiles.get(1);

            assertNotNull(notFound);
            assertEquals("idTwo", notFound.getId());
            assertNull(notFound.getFamilyName());
            assertNull(notFound.getGivenName());
            assertNull(notFound.getPrimaryEmail());

            assertNotNull(found);
            assertEquals("idOne", found.getId());
            assertEquals("Morgan", found.getFamilyName());
            assertEquals("Arthur", found.getGivenName());
            assertEquals("reddeadredemption@dev.dev", found.getPrimaryEmail());
        });
    }

    @Test
    void getAccountSummaryPayload() {
        final UUID accountIdOne = UUID.randomUUID();
        final String userIdOne = "userIdOne";
        final UUID accountIdTwo = UUID.randomUUID();
        final String userIdTwo = "userIdTwo";
        final List<IESAccountTracking> accountTrackings = Lists.newArrayList(new IESAccountTracking()
                .setAccountId(accountIdOne)
                .setIesUserId(userIdOne), new IESAccountTracking()
                .setAccountId(accountIdTwo)
                .setIesUserId(userIdTwo));

        final IdentityProfile identityOne = new IdentityProfile()
                .setFamilyName("Morgan")
                .setGivenName("Arthur")
                .setPrimaryEmail("arthur@rdrd.dev")
                .setId(userIdOne);
        final IdentityProfile identityTwo = new IdentityProfile()
                .setFamilyName("Marston")
                .setGivenName("John")
                .setPrimaryEmail("john@rdrd.dev")
                .setId(userIdTwo);

        IESBatchProfileGetEventMessage message = new IESBatchProfileGetEventMessage(new IESBatchProfileGetParams(Lists.newArrayList(userIdOne, userIdTwo)));
        message.setIdentityProfile(Lists.newArrayList(identityOne, identityTwo));
        message.setNotFound(new ArrayList<>());
        when(camel.toStream(eq(IES_BATCH_PROFILE_GET), any(IESBatchProfileGetEventMessage.class), eq(IESBatchProfileGetEventMessage.class)))
                .thenReturn(Mono.just(message));

        final List<AccountSummaryPayload> payloads = iesService.getAccountSummaryPayload(accountTrackings)
                .collectList()
                .block();

        assertAll(() -> {
            assertNotNull(payloads);
            assertEquals(2 , payloads.size());
            final AccountSummaryPayload one = payloads.get(0);
            final AccountSummaryPayload two = payloads.get(1);

            assertNotNull(one);
            assertEquals(accountIdOne, one.getAccountId());
            assertEquals("Arthur", one.getGivenName());
            assertEquals("Morgan", one.getFamilyName());
            assertEquals("arthur@rdrd.dev", one.getPrimaryEmail());

            assertNotNull(two);
            assertEquals(accountIdTwo, two.getAccountId());
            assertEquals("John", two.getGivenName());
            assertEquals("Marston", two.getFamilyName());
            assertEquals("john@rdrd.dev", two.getPrimaryEmail());
        });
    }
}
