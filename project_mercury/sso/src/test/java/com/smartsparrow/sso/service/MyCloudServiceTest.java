package com.smartsparrow.sso.service;

import static com.smartsparrow.dataevent.RouteUri.MYCLOUD_TOKEN_VALIDATE;
import static com.smartsparrow.sso.service.MyCloudService.MYCLOUD_EMAIL_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.data.MyCloudAccountTracking;
import com.smartsparrow.iam.data.MyCloudAccountTrackingGateway;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.sso.event.MyCloudTokenValidationEventMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class MyCloudServiceTest {

    @InjectMocks
    private MyCloudService mycloudService;

    @Mock
    private AccountService accountService;

    @Mock
    private SubscriptionPermissionService subscriptionPermissionService;

    @Mock
    private CamelReactiveStreamsService camel;

    @Mock
    private MyCloudAccountTrackingGateway mycloudAccountTrackingGateway;

    private final static String token = "here is a token mate!";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateToken_nullToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> mycloudService.validateToken(null));

        assertNotNull(f);
        assertEquals("token is required", f.getMessage());
    }

    @Test
    void validateToken_emptyToken() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> mycloudService.validateToken(""));

        assertNotNull(f);
        assertEquals("token is required", f.getMessage());
    }

    @Test
    void validateToken_invalid() {
        when(camel.toStream(eq(MYCLOUD_TOKEN_VALIDATE), any(MyCloudTokenValidationEventMessage.class), eq(MyCloudTokenValidationEventMessage.class)))
                .thenReturn(Mono.just(new MyCloudTokenValidationEventMessage(token)));

        UnauthorizedFault f = assertThrows(UnauthorizedFault.class, () -> mycloudService.validateToken(token).block());

        assertNotNull(f);
        assertEquals("Invalid token supplied", f.getMessage());

        ArgumentCaptor<MyCloudTokenValidationEventMessage> captor = ArgumentCaptor
                .forClass(MyCloudTokenValidationEventMessage.class);

        verify(camel).toStream(eq(MYCLOUD_TOKEN_VALIDATE), captor.capture(), eq(MyCloudTokenValidationEventMessage.class));

        MyCloudTokenValidationEventMessage result = captor.getValue();

        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    void validateToken_valid() {
        final String pearsonUid = "pearsonUid";

        // mock the route validating the token
        MyCloudTokenValidationEventMessage eventMessage = new MyCloudTokenValidationEventMessage(token);
        eventMessage.markValid();
        eventMessage.setPearsonUid(pearsonUid);

        when(camel.toStream(eq(MYCLOUD_TOKEN_VALIDATE), any(MyCloudTokenValidationEventMessage.class), eq(MyCloudTokenValidationEventMessage.class)))
                .thenReturn(Mono.just(eventMessage));

        String uid = mycloudService.validateToken(token).block();

        assertEquals(pearsonUid, uid);

        ArgumentCaptor<MyCloudTokenValidationEventMessage> captor = ArgumentCaptor
                .forClass(MyCloudTokenValidationEventMessage.class);

        verify(camel).toStream(eq(MYCLOUD_TOKEN_VALIDATE), captor.capture(), eq(MyCloudTokenValidationEventMessage.class));

        MyCloudTokenValidationEventMessage result = captor.getValue();

        assertNotNull(result);
        // token was false before reaching the route
        assertFalse(result.isValid());
    }

    @Test
    void findAccount_notFound() {
        final String pearsonUid = "pearsonUid";

        when(mycloudAccountTrackingGateway.findAccountId(pearsonUid)).thenReturn(Mono.empty());

        assertNull(mycloudService.findAccount(pearsonUid).block());

        verify(mycloudAccountTrackingGateway).findAccountId(pearsonUid);
        verify(accountService, never()).findById(any(UUID.class));
    }

    @Test
    void findAccount_found() {
        final String pearsonUid = "pearsonUid";
        final UUID accountId = UUID.randomUUID();
        when(mycloudAccountTrackingGateway.findAccountId(pearsonUid))
                .thenReturn(Mono.just(new MyCloudAccountTracking()
                        .setMyCloudUserId(pearsonUid)
                        .setAccountId(accountId)));

        when(accountService.findById(accountId))
                .thenReturn(Flux.just(new Account()));

        assertNotNull(mycloudService.findAccount(pearsonUid).block());

        verify(mycloudAccountTrackingGateway).findAccountId(pearsonUid);
        verify(accountService).findById(accountId);
    }

    @Test
    void provisionAccount_emptyPearsonUid() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> mycloudService.provisionAccount("")
                .block());

        assertNotNull(f);
        assertEquals("pearsonUid is required", f.getMessage());
    }

    @Test
    void provisionAccount_conflictException() {
        final String pearsonUid =" pearsonUid";
        doThrow(new ConflictException("conflict"))
                .when(accountService).provision(AccountProvisionSource.OIDC, null, null,
                                                null, null, String.format(MYCLOUD_EMAIL_FORMAT, pearsonUid), null,
                                                null, null, true, AuthenticationType.MYCLOUD);

        IllegalStateFault f = assertThrows(IllegalStateFault.class, () -> mycloudService.provisionAccount(pearsonUid));

        assertNotNull(f);
        assertEquals("conflict", f.getMessage());
    }

    @Test
    void provisionAccount() {
        final String pearsonUid = "pearsonUid";
        final UUID accountId = UUID.randomUUID();
        final UUID subscriptionId = UUID.randomUUID();

        when(accountService.provision(AccountProvisionSource.OIDC, null, null,
                null, null, String.format(MYCLOUD_EMAIL_FORMAT, pearsonUid), null,
                null, null, true, AuthenticationType.MYCLOUD)).thenReturn(new AccountAdapter()
                .setAccount(new Account()
                        .setSubscriptionId(subscriptionId)
                        .setId(accountId)));

        when(subscriptionPermissionService.saveAccountPermission(accountId, subscriptionId, PermissionLevel.OWNER))
                .thenReturn(Flux.just(new Void[]{}));

        when(mycloudAccountTrackingGateway.persist(any(MyCloudAccountTracking.class)))
                .thenReturn(Flux.just(new Void[]{}));

        Account account = mycloudService.provisionAccount(pearsonUid)
                .block();

        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals(subscriptionId, account.getSubscriptionId());

        ArgumentCaptor<MyCloudAccountTracking> captor = ArgumentCaptor.forClass(MyCloudAccountTracking.class);

        verify(subscriptionPermissionService).saveAccountPermission(accountId, subscriptionId, PermissionLevel.OWNER);
        verify(mycloudAccountTrackingGateway).persist(captor.capture());

        MyCloudAccountTracking iesAccountTracking = captor.getValue();

        assertNotNull(iesAccountTracking);
        assertEquals(pearsonUid, iesAccountTracking.getMyCloudUserId());
        assertEquals(accountId, iesAccountTracking.getAccountId());
    }
}
