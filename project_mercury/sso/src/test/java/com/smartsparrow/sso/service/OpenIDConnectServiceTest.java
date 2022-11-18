package com.smartsparrow.sso.service;

import static com.smartsparrow.sso.service.OpenIDConnectStubs.AUTH_ENDPOINT;
import static com.smartsparrow.sso.service.OpenIDConnectStubs.CALLBACK_URL;
import static com.smartsparrow.sso.service.OpenIDConnectStubs.ISSUER;
import static com.smartsparrow.sso.service.OpenIDConnectStubs.mockDiscoveryDocumentCache;
import static com.smartsparrow.sso.service.OpenIDConnectStubs.mockOpenIDConnectConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.data.FederatedIdentity;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.ClaimService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.sso.data.oidc.AccessTokenByWebSessionTokenGateway;
import com.smartsparrow.sso.data.oidc.RelyingPartyCredentialGateway;
import com.smartsparrow.sso.data.oidc.SessionAccountGateway;
import com.smartsparrow.sso.data.oidc.StateGateway;
import com.smartsparrow.sso.wiring.OpenIDConnectConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class OpenIDConnectServiceTest {

    private static final UUID subscriptionId = UUID.randomUUID();
    private static final UUID relyingPartyId = UUID.randomUUID();
    private static final String clientId = "599996276248-bctjf9e8la9ngt5jd6ste7lphf2e1oph.apps.googleusercontent.com";
    private static final String clientSecret = "dYlNUl-U-zy8gvkpGjnnTDS3";
    private static final String JWT_SUBJECT = "jwtSubject";
    private static final String JWT_EMAIL = "jwt@email.com";
    private static final String JWT_GIVEN_NAME = "jwtGivenName";
    private static final String JWT_FAMILY_NAME = "jwtFamilyName";
    private static final String oneTimeAuthCode = "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7";
    private static final String invalidateBearerToken = "invalidateThis";

    @InjectMocks
    private OpenIDConnectService openIDConnectService;

    @Mock
    private DiscoveryDocumentCache discoveryDocumentCache;
    @Mock
    private OpenIDConnectConfig openIDConnectConfig;
    @Mock
    private AccountService accountService;
    @Mock
    private CredentialService credentialService;
    @Mock
    private RelyingPartyCredentialGateway relyingPartyCredentialGateway;
    @Mock
    private StateGateway stateGateway;
    @Mock
    private OpenIDConnectLogService openIDConnectLogService;
    @Mock
    private SessionAccountGateway sessionAccountGateway;
    @Mock
    private ClaimService claimService;
    @Mock
    private AccessTokenByWebSessionTokenGateway accessTokenByWebSessionTokenGateway;

    private OpenIDConnectService openIDConnectServiceSpy;

    @BeforeEach
    void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        mockDiscoveryDocumentCache(discoveryDocumentCache);
        mockOpenIDConnectConfig(openIDConnectConfig);

        when(relyingPartyCredentialGateway.find(relyingPartyId)).thenReturn(Mono.just(credential));
        when(stateGateway.persist(any())).thenReturn(Flux.empty());
        when(stateGateway.find("_state_")).thenReturn(Mono.just(state));
        when(accountService.provision(eq(AccountProvisionSource.OIDC), any(), any(), any(), eq(JWT_GIVEN_NAME),
                eq(JWT_FAMILY_NAME), any(), eq(JWT_EMAIL), any(), any(), any(), any(), any()))
                .thenReturn(new AccountAdapter().setAccount(account));
        when(accountService.addFederation(credential.getSubscriptionId(), //
                credential.getClientId(), //
                JWT_SUBJECT, //
                account.getId())).thenReturn(Mono.just(new FederatedIdentity()));
        when(openIDConnectLogService.logError(any(), any(), any())).thenReturn(Flux.empty());
        when(openIDConnectLogService.logEvent(any(), any())).thenReturn(Flux.empty());
        when(openIDConnectLogService.logEvent(any(), any(), any())).thenReturn(Flux.empty());
        when(sessionAccountGateway.persist(any())).thenReturn(Flux.empty());
        when(accessTokenByWebSessionTokenGateway.persist(any())).thenReturn(Flux.empty());

        openIDConnectServiceSpy = Mockito.spy(openIDConnectService);
    }

    private OpenIDConnectRelyingPartyCredential credential = new OpenIDConnectRelyingPartyCredential()
            .setRelyingPartyId(relyingPartyId)
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setIssuerUrl(ISSUER)
            .setSubscriptionId(subscriptionId)
            .setAuthenticationRequestScope("openid email profile")
            .setLogDebug(false)
            .setEnforceVerifiedEmail(true);

    private OpenIDConnectState state = new OpenIDConnectState()
            .setRelyingPartyId(relyingPartyId)
            .setRedirectUrl("redirectUrl")
            .setState("_state_")
            .setNonce("_nonce_");

    private Account account = new Account().setId(UUID.randomUUID());
    private AccountIdentityAttributes identity = new AccountIdentityAttributes()
            .setAccountId(account.getId())
            .setHonorificPrefix("Pr")
            .setHonorificSuffix("Sr")
            .setFamilyName("Old surname")
            .setGivenName("Old name")
            .setEmail(Sets.newHashSet("old@email.com"));

    @Test
    void buildAuthenticationRequest() {

        URI result = openIDConnectService.buildAuthenticationRequest(relyingPartyId, "index.html",
                                                                     invalidateBearerToken);

        //verify that all needed fields are presented in authntication request
        assertNotNull(result);
        assertEquals(URI.create(AUTH_ENDPOINT).getHost(), result.getHost());
        assertEquals(URI.create(AUTH_ENDPOINT).getPath(), result.getPath());

        Map<String, String> params = Maps.newHashMap();
        for (String param : result.getQuery().split("&")) {
            String[] p = param.split("=");
            params.put(p[0], p[1]);
        }

        assertNotNull(params.get("state"));
        assertNotNull(params.get("nonce"));
        assertEquals(credential.getClientId(), params.get("client_id"));
        assertEquals("code", params.get("response_type"));
        assertEquals(CALLBACK_URL, params.get("redirect_uri"));
        assertEquals("openid+email+profile", params.get("scope"));

        //verify that state was saved
        ArgumentCaptor<OpenIDConnectState> captor = ArgumentCaptor.forClass(OpenIDConnectState.class);
        verify(stateGateway).persist(captor.capture());
        assertEquals(relyingPartyId, captor.getValue().getRelyingPartyId());
        assertEquals(params.get("nonce"), captor.getValue().getNonce());
        assertEquals("index.html", captor.getValue().getRedirectUrl());
        assertEquals(params.get("state"), captor.getValue().getState());
    }

    @Test
    void processFederatedAuthentication_incorrectNonce() throws ParseException {
        OIDCTokenResponse responseMock = mockOIDCTokenResponse();
        when(responseMock.getOIDCTokens().getIDToken().getJWTClaimsSet().getStringClaim("nonce")).thenReturn("incorrectNonce");
        Mockito.doReturn(responseMock).when(openIDConnectServiceSpy).performTokenRequest(credential, oneTimeAuthCode, "_state_");

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> openIDConnectServiceSpy.processCallback(oneTimeAuthCode, "_state_"));
        assertEquals("nonce mismatch", fault.getMessage());
    }

    @Test
    void processFederatedAuthentication_success() throws ParseException {
        OIDCTokenResponse responseMock = mockOIDCTokenResponse();
        Mockito.doReturn(responseMock).when(openIDConnectServiceSpy).performTokenRequest(credential, oneTimeAuthCode, "_state_");
        Mockito.doReturn(account).when(openIDConnectServiceSpy).getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME,
                JWT_FAMILY_NAME, true, JWT_EMAIL, JWT_SUBJECT);
        Mockito.doNothing().when(openIDConnectServiceSpy).updateAccountProperties(credential, account, JWT_GIVEN_NAME,
                JWT_FAMILY_NAME, true, JWT_EMAIL);
        when(credentialService.createWebSessionToken(eq(account.getId()), any(), any())).thenReturn(Mono.just(new WebSessionToken().setToken("token")));

        WebSessionToken result = openIDConnectServiceSpy.processCallback(oneTimeAuthCode, "_state_");

        assertNotNull(result);
        assertEquals("token", result.getToken());
    }

    @Test
    void processFederatedAuthentication_invalidState() {
        when(stateGateway.find("_state_")).thenReturn(Mono.empty());

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                () -> openIDConnectService.processCallback(oneTimeAuthCode, "_state_"));
        assertEquals("invalid state supplied", fault.getMessage());
    }

    @Test
    void processCallback_noNames() throws Exception {

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class,
                                                  () -> processCallback_testNames(null, null));

        assertEquals("missing required 'given_name' and/or 'family_name' claims", fault.getMessage());
    }

    @Test
    void processCallback_onlyFamilyName() throws Exception {
        WebSessionToken result = processCallback_testNames(null, JWT_FAMILY_NAME);

        assertNotNull(result);
        assertEquals("token", result.getToken());
    }

    @Test
    void processCallback_onlyGivenName() throws Exception {
        WebSessionToken result = processCallback_testNames(JWT_GIVEN_NAME, null);

        assertNotNull(result);
        assertEquals("token", result.getToken());
    }

    private WebSessionToken processCallback_testNames(final String jwtGivenName,
                                                      final String jwtFamilyName) throws Exception {
        OIDCTokenResponse responseMock = mockOIDCTokenResponse(jwtGivenName, jwtFamilyName);
        Mockito.doReturn(responseMock).when(openIDConnectServiceSpy).performTokenRequest(credential,
                                                                                         oneTimeAuthCode,
                                                                                         "_state_");
        Mockito.doReturn(account).when(openIDConnectServiceSpy).getFederatedAccount(credential,
                                                                                    "_state_",
                                                                                    jwtGivenName,
                                                                                    jwtFamilyName,
                                                                                    true,
                                                                                    JWT_EMAIL,
                                                                                    JWT_SUBJECT);
        Mockito.doNothing().when(openIDConnectServiceSpy).updateAccountProperties(credential, account, jwtGivenName,
                                                                                  jwtFamilyName, true, JWT_EMAIL);
        when(credentialService.createWebSessionToken(eq(account.getId()),
                                                     any(),
                                                     any())).thenReturn(Mono.just(new WebSessionToken().setToken("token")));

        WebSessionToken result = openIDConnectServiceSpy.processCallback(oneTimeAuthCode, "_state_");

        // assertions performed by caller.
        return result;
    }

    @Test
    void getFederatedAccount_federationExists() {
        when(accountService.findByFederation(any(), any(), any())).thenReturn(Mono.just(account));

        Account result = openIDConnectService.getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME, JWT_FAMILY_NAME,
                true, JWT_EMAIL, JWT_SUBJECT);

        assertEquals(account, result);
    }

    @Test
    void getFederatedAccount_existsByEmail() {
        when(accountService.findByFederation(any(), any(), any())).thenReturn(Mono.empty());
        when(accountService.findByEmail(JWT_EMAIL)).thenReturn(Flux.just(account));

        Account result = openIDConnectService.getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME, JWT_FAMILY_NAME,
                true, JWT_EMAIL, JWT_SUBJECT);

        assertEquals(account, result);
        verify(accountService).addFederation(any(), any(), eq(JWT_SUBJECT), eq(account.getId()));
    }

    @Test
    void getFederatedAccount_emailIsNotVerified() {
        when(accountService.findByFederation(any(), any(), any())).thenReturn(Mono.empty());
        when(accountService.findByEmail(JWT_EMAIL)).thenReturn(Flux.empty());

        IllegalArgumentFault fault = assertThrows(IllegalArgumentFault.class, () ->
                openIDConnectService.getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME, JWT_FAMILY_NAME,
                        false, JWT_EMAIL, JWT_SUBJECT));

        assertEquals("email address must be verified for account provision", fault.getMessage());
    }

    @Test
    void getFederatedAccount_emailIsNotVerified_doNotEnforceEmailVerification() {
        when(accountService.findByFederation(any(), any(), any())).thenReturn(Mono.empty());
        when(accountService.findByEmail(JWT_EMAIL)).thenReturn(Flux.empty());
        credential.setEnforceVerifiedEmail(false);

        Account result = openIDConnectService.getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME, JWT_FAMILY_NAME,
                false, JWT_EMAIL, JWT_SUBJECT);

        assertEquals(account, result);
        verify(accountService).addFederation(any(), any(), eq(JWT_SUBJECT), eq(account.getId()));
    }

    @Test
    void getFederatedAccount_provision() {
        when(accountService.findByFederation(any(), any(), any())).thenReturn(Mono.empty());
        when(accountService.findByEmail(JWT_EMAIL)).thenReturn(Flux.empty());

        Account result = openIDConnectService.getFederatedAccount(credential, "_state_", JWT_GIVEN_NAME, JWT_FAMILY_NAME,
                true, JWT_EMAIL, JWT_SUBJECT);

        assertEquals(account, result);
        verify(accountService).addFederation(any(), any(), eq(JWT_SUBJECT), eq(account.getId()));
    }

    @Test
    void updateAccountProperties_addIdentities() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.empty());

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, JWT_FAMILY_NAME, true, JWT_EMAIL);

        verify(accountService).setIdentityNames(account.getId(), null, JWT_GIVEN_NAME, JWT_FAMILY_NAME, null);
        verify(accountService).addVerifiedEmail(account.getId(), JWT_EMAIL);
    }

    @Test
    void updateAccountProperties_addEmail() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.empty());

        openIDConnectService.updateAccountProperties(credential, account, null, JWT_FAMILY_NAME, true, JWT_EMAIL);

        verify(accountService, never()).setIdentityNames(any(), any(), any(), any(), any());
        verify(accountService).addVerifiedEmail(account.getId(), JWT_EMAIL);
    }

    @Test
    void updateAccountProperties_emailIsNotVerified() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.empty());

        openIDConnectService.updateAccountProperties(credential, account, null, JWT_FAMILY_NAME, false, JWT_EMAIL);

        verify(accountService, never()).setIdentityNames(any(), any(), any(), any(), any());
        verify(accountService, never()).addVerifiedEmail(any(), any());
    }

    @Test
    void updateAccountProperties_emailIsNotVerified_noEmailVerification() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.empty());
        credential.setEnforceVerifiedEmail(false);

        openIDConnectService.updateAccountProperties(credential, account, null, JWT_FAMILY_NAME, false, JWT_EMAIL);

        verify(accountService, never()).setIdentityNames(any(), any(), any(), any(), any());
        verify(accountService).addVerifiedEmail(account.getId(), JWT_EMAIL);
    }

    @Test
    void updateAccountProperties_nothingToAdd() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.empty());

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, null, false, JWT_EMAIL);

        verify(accountService, never()).setIdentityNames(any(), any(), any(), any(), any());
        verify(accountService, never()).addVerifiedEmail(any(), any());
    }

    @Test
    void updateAccountProperties_updateName() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.just(identity));

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, JWT_FAMILY_NAME, false, JWT_EMAIL);

        verify(accountService).setIdentityNames(account.getId(), identity.getHonorificPrefix(), JWT_GIVEN_NAME,
                JWT_FAMILY_NAME, identity.getHonorificSuffix());
        verify(accountService, never()).addVerifiedEmail(any(), any());
    }

    @Test
    void updateAccountProperties_updateEmail() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.just(identity));

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, JWT_FAMILY_NAME, true, JWT_EMAIL);

        verify(accountService).addVerifiedEmail(account.getId(), JWT_EMAIL);
    }

    @Test
    void updateAccountProperties_doNotUpdateEmail() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.just(identity));

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, JWT_FAMILY_NAME, false, JWT_EMAIL);

        verify(accountService, never()).addVerifiedEmail(any(), any());
    }

    @Test
    void updateAccountProperties_updateEmail_noEmailVerification() {
        when(accountService.findIdentityByAccount(account)).thenReturn(Flux.just(identity));
        credential.setEnforceVerifiedEmail(false);

        openIDConnectService.updateAccountProperties(credential, account, JWT_GIVEN_NAME, JWT_FAMILY_NAME, false, JWT_EMAIL);

        verify(accountService).addVerifiedEmail(account.getId(), JWT_EMAIL);
    }


    private OIDCTokenResponse mockOIDCTokenResponse() throws ParseException {
        return mockOIDCTokenResponse(JWT_GIVEN_NAME, JWT_FAMILY_NAME);
    }

    private OIDCTokenResponse mockOIDCTokenResponse(String given_name, String family_name) throws ParseException {
        OIDCTokenResponse responseMock = mock(OIDCTokenResponse.class);
        OIDCTokens tokensMock = mock(OIDCTokens.class);
        when(responseMock.getOIDCTokens()).thenReturn(tokensMock);
        JWT jwtMock = mock(JWT.class);
        when(tokensMock.getIDToken()).thenReturn(jwtMock);
        JWTClaimsSet setMock = mock(JWTClaimsSet.class);
        when(jwtMock.getJWTClaimsSet()).thenReturn(setMock);
        when(setMock.getStringClaim("nonce")).thenReturn(state.getNonce());
        when(setMock.getStringClaim("given_name")).thenReturn(given_name);
        when(setMock.getStringClaim("family_name")).thenReturn(family_name);
        when(setMock.getStringClaim("email")).thenReturn(JWT_EMAIL);
        when(setMock.getBooleanClaim("email_verified")).thenReturn(true);
        when(setMock.getStringClaim("sub")).thenReturn(JWT_SUBJECT);

        // stub in the access token to the response.
        com.nimbusds.oauth2.sdk.token.AccessToken responseAccessToken = mock(AccessToken.class);
        when(responseAccessToken.getType()).thenReturn(AccessTokenType.BEARER);
        when(tokensMock.getAccessToken()).thenReturn(responseAccessToken);

        return responseMock;
    }


}
