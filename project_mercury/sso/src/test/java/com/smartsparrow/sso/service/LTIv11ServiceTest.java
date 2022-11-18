package com.smartsparrow.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.sso.data.ltiv11.LTI11LaunchSessionHash;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.data.ltiv11.LTIv11Gateway;
import com.smartsparrow.util.UUIDs;

import io.reactivex.functions.Function3;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LTIv11ServiceTest {

    @InjectMocks
    private LTIv11Service ltIv11Service;

    @Mock
    private LTIv11Gateway ltIv11Gateway;

    @Mock
    private AccountService accountService;

    @Mock
    private IESService iesService;

    //


    private static final Account nonFederatedAccount = new Account()
            .setId(UUIDs.timeBased());

    @BeforeEach
    public void setup() throws Exception {
        // create any @Mock
        MockitoAnnotations.openMocks(this);

        //
        when(ltIv11Gateway.persist(any(LTILaunchRequestEntry.class))).thenReturn(Flux.empty());
        when(ltIv11Gateway.persist(any(LTILaunchRequestLogEvent.class))).thenReturn(Flux.empty());
        when(ltIv11Gateway.persistLaunchRequestStaticFields(any(LTILaunchRequestEntry.class))).thenReturn(Flux.empty());
        when(ltIv11Gateway.associateLaunchRequestToAccount(any(UUID.class), any(UUID.class))).thenReturn(Flux.empty());

        when(iesService.findAccount(any(String.class)))
                .thenReturn(Mono.just(nonFederatedAccount));

        when(iesService.provisionAccount(any(String.class), eq(AccountProvisionSource.LTI), any()))
                .thenReturn(Mono.just(nonFederatedAccount));
    }

    @Test
    void createLTIConsumerKey() {
        UUID subscriptionId = UUID.randomUUID();
        when(ltIv11Gateway.save(any())).thenReturn(Flux.empty());
        when(ltIv11Gateway.findLTIConsumerKey(anyString())).thenReturn(Mono.empty());

        LTIConsumerKey result = ltIv11Service.createLTIConsumerKey(subscriptionId, null);

        assertNotNull(result.getKey());
        assertEquals(subscriptionId, result.getSubscriptionId());
        assertNotNull(result.getSecret());
        verify(ltIv11Gateway, times(1)).save(eq(result));
    }

    @Test
    void createLTIConsumerKey_collision() {
        UUID subscriptionId = UUID.randomUUID();
        when(ltIv11Gateway.save(any())).thenReturn(Flux.empty());
        // mock a previously created consumer key.
        when(ltIv11Gateway.findLTIConsumerKey(anyString())).thenReturn(Mono.just(new LTIConsumerKey()));

        assertThrows(IllegalStateFault.class, () -> ltIv11Service.createLTIConsumerKey(subscriptionId, null));
    }

    @Test
    void createLTIConsumerKey_noSubscriptionId() {
        assertThrows(IllegalArgumentFault.class, () -> ltIv11Service.createLTIConsumerKey(null, null));
    }

    @Test
    void findAccountByLTIConfiguration_noConfigurationId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.findAccountByLTIConfiguration(null, null));

        assertEquals("configurationId is required", f.getMessage());
    }

    @Test
    void findAccountByLTIConfiguration_noUserId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.findAccountByLTIConfiguration(UUID.randomUUID(), null));

        assertEquals("userId is required", f.getMessage());
    }

    @Test
    void findAccountByLTIConfiguration() {
        final UUID accountId = UUID.randomUUID();
        final UUID configurationId = UUID.randomUUID();
        when(ltIv11Gateway.fetchAccountId(eq(configurationId), anyString()))
                .thenReturn(Mono.just(accountId));
        when(accountService.findById(accountId))
                .thenReturn(Flux.just(new Account()));

        Account acc = ltIv11Service.findAccountByLTIConfiguration(configurationId, "userId")
                .block();

        assertNotNull(acc);
        verify(ltIv11Gateway).fetchAccountId(configurationId, "userId");
        verify(accountService).findById(accountId);
    }

    @Test
    void findLTIConsumerConfiguration_noWorkspaceId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.findLTIConsumerConfiguration(null));

        assertEquals("workspaceId is required", f.getMessage());
    }

    @Test
    void findLTIConsumerConfiguration() {
        final UUID workspaceId = UUID.randomUUID();

        when(ltIv11Gateway.findConfigurationByWorkspace(workspaceId))
                .thenReturn(Mono.just(new LTIv11ConsumerConfiguration()));

        LTIv11ConsumerConfiguration conf = ltIv11Service.findLTIConsumerConfiguration(workspaceId)
                .block();

        assertNotNull(conf);

        verify(ltIv11Gateway).findConfigurationByWorkspace(workspaceId);
    }

    @Test
    void associateAccountIdToLTI_noConfigurationId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.associateAccountIdToLTI(null, null, null));

        assertEquals("configurationId is required", f.getMessage());
    }

    @Test
    void associateAccountIdToLTI_noUserId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.associateAccountIdToLTI(UUID.randomUUID(), null, null));

        assertEquals("userId is required", f.getMessage());
    }

    @Test
    void associateAccountIdToLTI_noAccountId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> ltIv11Service.associateAccountIdToLTI(UUID.randomUUID(), "id", null));

        assertEquals("accountId is required", f.getMessage());
    }

    @Test
    void associateAccountIdToLTI() {
        final UUID configurationId = UUID.randomUUID();
        final UUID accountId = UUID.randomUUID();
        final String userId = "id";

        when(ltIv11Gateway.persistAccountIdByLTI(configurationId, userId, accountId))
                .thenReturn(Flux.just(new Void[]{}));

        ltIv11Service.associateAccountIdToLTI(configurationId, userId, accountId)
                .blockLast();

        verify(ltIv11Gateway).persistAccountIdByLTI(configurationId, userId, accountId);
    }

    @Test
    void provisionAccount_sessionHashNotFound() {
        final String hash = "hash";
        final String pearsonUid = "userId";
        final UUID launchRequestId = UUID.randomUUID();
        when(ltIv11Gateway.findSessionHash(hash, launchRequestId))
                .thenReturn(Mono.empty());

        Function3<UUID, UUID, String, Mono<UUID>> enrolFunction = (uuid, uuid2, s) -> Mono.just(UUID.randomUUID());


        UnauthorizedFault f = assertThrows(UnauthorizedFault.class, () -> ltIv11Service
                .provisionAccount(hash, launchRequestId, pearsonUid, enrolFunction)
                .block());

        assertEquals("invalid session hash launch", f.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void provisionAccount() throws Exception {
        final String hash = "hash";
        final String pearsonUid = "userId";
        final UUID launchRequestId = UUID.randomUUID();
        final UUID accountId = UUID.randomUUID();

        ArgumentCaptor<LTI11LaunchSessionHash> hashCaptor = ArgumentCaptor.forClass(LTI11LaunchSessionHash.class);
        ArgumentCaptor<LTILaunchRequestLogEvent> eventCaptor = ArgumentCaptor.forClass(LTILaunchRequestLogEvent.class);
        LTI11LaunchSessionHash sessionHash = new LTI11LaunchSessionHash()
                .setHash(hash)
                .setCohortId(UUID.randomUUID())
                .setConfigurationId(UUID.randomUUID())
                .setContinueTo("http://some-tld.pearson.com/foo")
                .setLaunchRequestId(launchRequestId)
                .setStatus(LTI11LaunchSessionHash.Status.VALID)
                .setUserId("ltiUserId");
        when(ltIv11Gateway.findSessionHash(hash, launchRequestId))
                .thenReturn(Mono.just(sessionHash));

        when(iesService.findAccount(pearsonUid)).thenReturn(Mono.empty());

        Function3<UUID, UUID, String, Mono<UUID>> enrolFunction = mock(Function3.class);//(uuid, uuid2, s) -> Mono.just(UUID.randomUUID());
        when(enrolFunction.apply(accountId, sessionHash.getCohortId(), pearsonUid))
                .thenReturn(Mono.just(UUID.randomUUID()));

        when(iesService.provisionAccount(pearsonUid, AccountProvisionSource.LTI, AuthenticationType.LTI))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)));
        when(ltIv11Gateway.persistAccountIdByLTI(sessionHash.getConfigurationId(), sessionHash.getUserId(), accountId))
                .thenReturn(Flux.just(new Void[]{}));
        when(ltIv11Gateway.persist(any(LTILaunchRequestLogEvent.class))).thenReturn(Flux.just(new Void[]{}));
        when(ltIv11Gateway.persist(any(LTI11LaunchSessionHash.class))).thenReturn(Mono.just(new LTI11LaunchSessionHash()));

        LTI11LaunchSessionHash updatedSessionHash = ltIv11Service.provisionAccount(hash, launchRequestId, pearsonUid, enrolFunction)
                .block();

        assertNotNull(updatedSessionHash);

        verify(ltIv11Gateway).findSessionHash(hash, launchRequestId);
        verify(iesService).provisionAccount(pearsonUid, AccountProvisionSource.LTI, AuthenticationType.LTI);
        verify(ltIv11Gateway).persistAccountIdByLTI(sessionHash.getConfigurationId(), sessionHash.getUserId(), accountId);
        verify(ltIv11Gateway).persist(hashCaptor.capture());
        verify(ltIv11Gateway).persist(eventCaptor.capture());
        verify(enrolFunction).apply(accountId, sessionHash.getCohortId(), pearsonUid);

        LTI11LaunchSessionHash updatedHash = hashCaptor.getValue();

        assertNotNull(updatedHash);
        assertEquals(LTI11LaunchSessionHash.Status.EXPIRED, updatedHash.getStatus());
        assertEquals(sessionHash.getHash(), updatedHash.getHash());
        assertEquals(sessionHash.getContinueTo(), updatedHash.getContinueTo());
        assertEquals(sessionHash.getUserId(), updatedHash.getUserId());
        assertEquals(sessionHash.getConfigurationId(), updatedHash.getConfigurationId());
        assertEquals(sessionHash.getLaunchRequestId(), updatedHash.getLaunchRequestId());
        assertEquals(sessionHash.getCohortId(), updatedHash.getCohortId());

        LTILaunchRequestLogEvent event = eventCaptor.getValue();

        assertNotNull(event);
        assertEquals(LTILaunchRequestLogEvent.Action.ACCOUNT_PROVISIONED, event.getAction());
    }

    @Test
    void findValidSessionHash_returnsEmptyWhenStatusExpired() {
        final String hash = "hash";
        final UUID launchRequestId = UUID.randomUUID();
        LTI11LaunchSessionHash sessionHash = new LTI11LaunchSessionHash()
                .setHash(hash)
                .setCohortId(UUID.randomUUID())
                .setConfigurationId(UUID.randomUUID())
                .setContinueTo("http://some-tld.pearson.com/foo")
                .setLaunchRequestId(launchRequestId)
                .setStatus(LTI11LaunchSessionHash.Status.EXPIRED)
                .setUserId("ltiUserId");
        when(ltIv11Gateway.findSessionHash(hash, launchRequestId))
                .thenReturn(Mono.just(sessionHash));

        LTI11LaunchSessionHash notFound = ltIv11Service.findValidSessionHash(hash, launchRequestId)
                .block();

        assertNull(notFound);
    }
}
