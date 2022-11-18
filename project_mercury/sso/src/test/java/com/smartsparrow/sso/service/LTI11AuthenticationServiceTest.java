package com.smartsparrow.sso.service;

import static com.smartsparrow.sso.service.LTIParam.LTI_MESSAGE_TYPE;
import static com.smartsparrow.sso.service.LTIParam.LTI_VERSION;
import static com.smartsparrow.sso.service.LTIParam.OAUTH_CONSUMER_KEY;
import static com.smartsparrow.sso.service.LTIParam.RESOURCE_LINK_ID;
import static com.smartsparrow.sso.service.LTIParam.USER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.IESAccountTracking;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.service.WebTokenType;
import com.smartsparrow.sso.data.ltiv11.LTIv11ConsumerConfiguration;
import com.smartsparrow.sso.lang.PIUserIdNotFoundException;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.Maps;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LTI11AuthenticationServiceTest {

    @InjectMocks
    private LTI11AuthenticationService authenticationService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private LTIv11Service ltIv11Service;

    @Mock
    private IESService iesService;

    @Mock
    private LTI11ConsumerCredentials credentials;

    @Mock
    private LTIMessage ltiMessage;

    private static final String url = "http://some.tld.pearson.com/";
    private static final String key = "key";
    private static final String secret = "secret";
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID launchRequestId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID consumerConfigurationId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final String userId = "userId";
    private static final String token = "eyJraWQiOiJrMzI4LjE1NjM5MTM0ODEiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJmZmZmZmZmZjVmMjBlMDlkMWQ0Y" +
            "jc0MDFkZmNjYmZiMiIsImhjYyI6IkFVIiwidHlwZSI6ImF0IiwiZXhwIjoxNTk3MTI0NjQxLCJpYXQiOjE1OTcxMjI4NDEsImNsaWVud" +
            "F9pZCI6Im9YNVZtNlNFRWVTaTVRQkVBVTAwODF0eDIwVUhFNDY5Iiwic2Vzc2lkIjoiNzY1ZmVjZDAtMWU5ZC00ZDkxLWEzNDktMDI2N" +
            "zRkNzRkNGI4In0.Bd89NMcbydGhzv_QkP-rCXUqNNrPhl9qwXQ0czz_cKgsI66Bqi9aAcaTGeSz2awbFlOzDfrYm9fkwrlbq0yaeowjo" +
            "SVw6BAhXct_vqv83_agcY3w5fhJmpl-gUL4wj3uZIg8uKHXBF8fhjaNLdIO9HmahwAocSpH71EtLOD62nnGv3EmsF9Hzw0abpPGMSF9g" +
            "DUqRS3rjXzrAkjRzX9CX1A_odYPkP65UYSgHNfVKP7jjJHS1x-v2um6GpX435RO-F38LPRy336mEeoGoZv6X9q6i5JA5H2dauhLna728" +
            "q3Fmg2kKsLlvGi148JM4wNb6-DzxBnq_LyES0e7Iwkg1w";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(ltiMessage.get(OAUTH_CONSUMER_KEY)).thenReturn(key);
        when(ltiMessage.get(LTI_VERSION)).thenReturn(Enums.asString(LTIVersion.VERSION_1_1));
        when(ltiMessage.get(LTI_MESSAGE_TYPE)).thenReturn("type");
        when(ltiMessage.get(RESOURCE_LINK_ID)).thenReturn("linkId");
        when(ltiMessage.get(USER_ID)).thenReturn(userId);
        when(ltiMessage.getParams()).thenReturn(Maps.of("placeholder", "placeholder"));

        when(credentials.getUrl()).thenReturn(url);
        when(credentials.getLtiMessage()).thenReturn(ltiMessage);
        when(credentials.getCohortId()).thenReturn(cohortId);
        when(credentials.getHttpHeaders()).thenReturn(new HashMap<>());
        when(credentials.getInvalidateBearerToken()).thenReturn(null);
        when(credentials.getKey()).thenReturn(key);
        when(credentials.getSecret()).thenReturn(secret);
        when(credentials.getType()).thenReturn(AuthenticationType.LTI);
        when(credentials.getLTIVersion()).thenReturn(LTIVersion.VERSION_1_1);
        when(credentials.getWorkspaceId()).thenReturn(workspaceId);
        when(credentials.getPiToken()).thenReturn(token);

        when(ltIv11Service.recordLaunchRequest(credentials))
                .thenReturn(Mono.just(launchRequestId));

        when(ltIv11Service.updateLaunchRequestStatus(eq(launchRequestId), any(LTILaunchRequestLogEvent.Action.class), anyString()))
                .thenReturn(Mono.just(new LTILaunchRequestLogEvent()));

        when(credentialService.createWebSessionToken(accountId, subscriptionId, null))
                .thenReturn(Mono.just(new WebSessionToken()
                        .setToken("token")
                        .setValidUntilTs(123)));

        when(ltIv11Service.findLTIConsumerConfiguration(workspaceId))
                .thenReturn(Mono.just(new LTIv11ConsumerConfiguration()
                        .setWorkspaceId(workspaceId)
                        .setId(consumerConfigurationId)));

        when(iesService.findIESId(accountId)).thenReturn(Mono.just(new IESAccountTracking()
                .setAccountId(accountId)
                .setIesUserId(userId)));

    }

    @Test
    void authenticate_invalidMessage() {
        when(credentials.getLtiMessage()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing launch request message", f.getMessage());
    }

    @Test
    void authenticate_missingUrl() {
        when(credentials.getUrl()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing launch url", f.getMessage());
    }

    @Test
    void authenticate_noUserId() {
        when(ltiMessage.get(USER_ID)).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing required field user_id", f.getMessage());

    }

    @Test
    void authenticate_noMessageType() {
        when(ltiMessage.get(LTI_MESSAGE_TYPE)).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing required field lti_message_type", f.getMessage());
    }

    @Test
    void authenticate_noVersion() {
        when(ltiMessage.get(LTI_VERSION)).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing required field lti_version", f.getMessage());
    }

    @Test
    void authenticate_noResourceLink() {
        when(ltiMessage.get(RESOURCE_LINK_ID)).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("missing required field resource_link_id", f.getMessage());
    }

    @Test
    void authenticate_invalidSignature() {
        doThrow(new IllegalArgumentFault("error")).when(ltIv11Service).assertValidSignature(credentials);
        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)
                        .setSubscriptionId(subscriptionId)));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("error", f.getMessage());
    }

    @Test
    void authenticate_invalidateBearerToken() {
        when(credentials.getInvalidateBearerToken()).thenReturn("invalidToken");
        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)
                        .setSubscriptionId(subscriptionId)));

        LTIWebSession session = authenticationService.authenticate(credentials)
                .block();

        assertNotNull(session);
        assertEquals(accountId, session.getAccount().getId());
        assertEquals(subscriptionId, session.getAccount().getSubscriptionId());
        assertEquals(WebTokenType.IES, session.getWebToken().getWebTokenType());

        IESWebToken token = (IESWebToken) session.getWebToken();

        assertEquals(123, token.getValidUntilTs());
        assertEquals(LTI11AuthenticationServiceTest.token, token.getToken());

        verify(iesService, never()).provisionAccount(anyString(), any(AccountProvisionSource.class), any());
        verify(ltIv11Service, times(1))
                .updateLaunchRequestStatus(eq(launchRequestId), eq(LTILaunchRequestLogEvent.Action.RECEIVED), eq("received"));
        verify(credentialService).invalidate("invalidToken");
    }

    @Test
    void authenticate_accountProvisioned() {
        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.empty());
        when(iesService.provisionAccount(anyString(), eq(AccountProvisionSource.LTI), any()))
                .thenReturn(Mono.just(new Account()
                .setId(accountId)
                .setSubscriptionId(subscriptionId)));
        when(ltIv11Service.associateAccountIdToLTI(consumerConfigurationId, userId, accountId))
                .thenReturn(Flux.just(new Void[]{}));

        when(iesService.findAccount(anyString())).thenReturn(Mono.empty());

        LTIWebSession session = authenticationService.authenticate(credentials)
                .block();

        assertNotNull(session);
        assertEquals(accountId, session.getAccount().getId());
        assertEquals(subscriptionId, session.getAccount().getSubscriptionId());
        assertEquals(WebTokenType.IES, session.getWebToken().getWebTokenType());

        IESWebToken token = (IESWebToken) session.getWebToken();

        assertEquals(123, token.getValidUntilTs());
        assertEquals(LTI11AuthenticationServiceTest.token, token.getToken());
        assertEquals(userId, token.getPearsonUid());

        verify(iesService).provisionAccount(anyString(), eq(AccountProvisionSource.LTI), any());
        verify(ltIv11Service, times(1))
                .updateLaunchRequestStatus(eq(launchRequestId), eq(LTILaunchRequestLogEvent.Action.RECEIVED), eq("received"));
        verify(ltIv11Service).associateAccountIdToLTI(consumerConfigurationId, userId, accountId);
        verify(ltIv11Service, never()).updateLaunchRequestStatus(any(UUID.class), eq(LTILaunchRequestLogEvent.Action.ERROR), anyString());

    }

    @Test
    void authenticate_piUserIdNotFound() {
        when(credentials.getPiToken()).thenReturn(null);

        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.empty());
        when(iesService.provisionAccount(anyString(), eq(AccountProvisionSource.LTI), any()))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)
                        .setSubscriptionId(subscriptionId)));
        when(ltIv11Service.associateAccountIdToLTI(consumerConfigurationId, userId, accountId))
                .thenReturn(Flux.just(new Void[]{}));

        PIUserIdNotFoundException ex = assertThrows(PIUserIdNotFoundException.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("could not provision the account, pi user id not found", ex.getMessage());
        assertEquals(launchRequestId, ex.getLaunchRequestId());
        assertEquals(userId, ex.getUserId());

        verify(ltIv11Service).updateLaunchRequestStatus(eq(launchRequestId), eq(LTILaunchRequestLogEvent.Action.ERROR), anyString());
    }

    @Test
    @DisplayName("It should throw when the account was already provisioned but the piUserId is not found")
    void authenticate_accountFound_noPiUserIdFound() {
        when(credentials.getPiToken()).thenReturn(null);
        when(credentialService.createWebSessionToken(accountId, subscriptionId, null))
                .thenReturn(Mono.just(new WebSessionToken()));

        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)
                        .setSubscriptionId(subscriptionId)));
        when(iesService.findIESId(accountId)).thenReturn(Mono.empty());

        PIUserIdNotFoundException ex = assertThrows(PIUserIdNotFoundException.class, () -> authenticationService.authenticate(credentials)
                .block());

        assertEquals("could not authenticate the account, pi user id not found", ex.getMessage());
        assertEquals(launchRequestId, ex.getLaunchRequestId());
        assertEquals(userId, ex.getUserId());

        verify(ltIv11Service).updateLaunchRequestStatus(eq(launchRequestId), eq(LTILaunchRequestLogEvent.Action.ERROR), anyString());
    }

    @Test
    @DisplayName("It should authenticate the user when the iesToken is missing but this lti user id already has a bronte account")
    void authenticate_accountFound() {
        when(credentials.getPiToken()).thenReturn(null);
        when(credentialService.createWebSessionToken(accountId, subscriptionId, null))
                .thenReturn(Mono.just(new WebSessionToken()));
        when(ltIv11Service.findAccountByLTIConfiguration(consumerConfigurationId, userId))
                .thenReturn(Mono.just(new Account()
                        .setId(accountId)
                        .setSubscriptionId(subscriptionId)));

        LTIWebSession session = authenticationService.authenticate(credentials)
                .block();

        assertNotNull(session);
        assertNotNull(session.getWebToken());
        assertEquals(WebTokenType.IES, session.getWebToken().getWebTokenType());
        IESWebToken iesWebToken = (IESWebToken) session.getWebToken();
        assertEquals(userId, iesWebToken.getPearsonUid());

        verify(ltIv11Service, never()).updateLaunchRequestStatus(any(UUID.class), eq(LTILaunchRequestLogEvent.Action.ERROR), anyString());

    }
}
