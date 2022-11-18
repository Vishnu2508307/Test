package com.smartsparrow.sso.service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.ACCOUNT_LOCATED_BY_EMAIL;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.ACCOUNT_LOCATED_BY_FEDERATION;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.ACCOUNT_PROVISIONED;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.ERROR;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.JWT_CLAIM;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.PROCESS_CALLBACK;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.PROCESS_CALLBACK_PARAMETERS;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.REDIRECT;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.RETRIEVE_METADATA;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.RETRIEVE_METADATA_RESULT;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.START;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.START_LOGOUT;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.SUCCESS;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_REQUEST;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_RESPONSE;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_RESPONSE_FAIL;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_RESPONSE_OK;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_RESPONSE_OK_RESULT;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_REVOKE_REQUEST;
import static com.smartsparrow.sso.service.OpenIDConnectLogEvent.Action.TOKEN_REVOKE_RESPONSE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;
import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.newrelic.api.agent.Trace;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.TokenRevocationRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.lang.WebTokenNotFoundFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.ClaimService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.sso.data.oidc.AccessTokenByWebSessionTokenGateway;
import com.smartsparrow.sso.data.oidc.RelyingPartyCredentialGateway;
import com.smartsparrow.sso.data.oidc.SessionAccountGateway;
import com.smartsparrow.sso.data.oidc.StateGateway;
import com.smartsparrow.sso.lang.OIDCTokenFault;
import com.smartsparrow.sso.lang.OIDCTokenParseFault;
import com.smartsparrow.sso.wiring.OpenIDConnectConfig;
import com.smartsparrow.util.Emails;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class OpenIDConnectService {

    private static final Logger log = LoggerFactory.getLogger(OpenIDConnectService.class);

    private final OpenIDConnectConfig openIDConnectConfig;
    private final DiscoveryDocumentCache discoveryDocumentCache;
    private final AccountService accountService;
    private final CredentialService credentialService;
    private final OpenIDConnectLogService openIDConnectLogService;
    private final ClaimService claimService;
    //
    private final StateGateway stateGateway;
    private final RelyingPartyCredentialGateway relyingPartyCredentialGateway;
    private final SessionAccountGateway sessionAccountGateway;
    private final AccessTokenByWebSessionTokenGateway accessTokenByWebSessionTokenGateway;

    // The default OIDC claims
    private static final Set<String> IGNORE_IAM_CLAIMS = Sets.newHashSet("email", //
                                                                         "name", //
                                                                         "family_name", //
                                                                         "given_name", //
                                                                         "middle_name", //
                                                                         "nickname", //
                                                                         "preferred_username", //
                                                                         "profile", //
                                                                         "website", //
                                                                         "gender", //
                                                                         "birthdate", //
                                                                         "zoneinfo", //
                                                                         "locale", //
                                                                         "updated_at", //
                                                                         "sub", // subject
                                                                         "aud", // audience
                                                                         "azp", //
                                                                         "nonce", //
                                                                         "auth_time", //
                                                                         "acr", //
                                                                         "iat", //
                                                                         "exp", //
                                                                         "at_hash", //
                                                                         "rt_hash");

    @Inject
    public OpenIDConnectService(OpenIDConnectConfig openIDConnectConfig,
            DiscoveryDocumentCache discoveryDocumentCache,
            AccountService accountService,
            CredentialService credentialService,
            OpenIDConnectLogService openIDConnectLogService,
            ClaimService claimService,
            StateGateway stateGateway,
            RelyingPartyCredentialGateway relyingPartyCredentialGateway,
            SessionAccountGateway sessionAccountGateway,
            AccessTokenByWebSessionTokenGateway accessTokenByWebSessionTokenGateway) {
        this.openIDConnectConfig = openIDConnectConfig;
        this.discoveryDocumentCache = discoveryDocumentCache;
        this.accountService = accountService;
        this.credentialService = credentialService;
        this.openIDConnectLogService = openIDConnectLogService;
        this.claimService = claimService;
        this.stateGateway = stateGateway;
        this.relyingPartyCredentialGateway = relyingPartyCredentialGateway;
        this.sessionAccountGateway = sessionAccountGateway;
        this.accessTokenByWebSessionTokenGateway = accessTokenByWebSessionTokenGateway;
    }

    /**
     * Get the callback URL for the OIDC connect service.
     * @return the callback URL
     */
    public URI getCallbackUri() {
        return URI.create(openIDConnectConfig.getCallbackUrl());
    }

    /**
     * Find a credential by using the relying party id.
     *
     * @param relyingPartyId the id of the credential
     * @return the credential or empty
     */
    public Mono<OpenIDConnectRelyingPartyCredential> findCredential(final UUID relyingPartyId) {
        return relyingPartyCredentialGateway.find(relyingPartyId);
    }

    /**
     * Add OpenID Connect credential
     *
     * @param subscriptionId the subscription id
     * @param issuerUrl      the issuer url
     * @param clientId       the client id
     * @param clientSecret   the client secret
     * @param requestScope   the request scope, if not provided the default value {@code "openid email profile"} will be saved
     * @return the credential
     */
    @Trace(async = true)
    public Mono<OpenIDConnectRelyingPartyCredential> addCredential(final UUID subscriptionId, final String issuerUrl,
            final String clientId,
            final String clientSecret,
            final String requestScope) {

        OpenIDConnectRelyingPartyCredential credential = new OpenIDConnectRelyingPartyCredential().setRelyingPartyId(
                UUIDs.timeBased()) //
                .setSubscriptionId(subscriptionId) //
                .setIssuerUrl(issuerUrl) //
                .setClientId(clientId) //
                .setClientSecret(clientSecret) //
                .setAuthenticationRequestScope(requestScope == null ? "openid email profile" : requestScope)
                .setLogDebug(false)
                .setEnforceVerifiedEmail(true);

        return relyingPartyCredentialGateway.persist(credential).then(Mono.just(credential)).doOnEach(
                ReactiveTransaction.linkOnNext());
    }

    /**
     * Build an Authentication Request as part of the code/authentication request flow.
     *
     * @param relyingPartyId the id
     * @param redirectToUrl the Url to redirect the user to at the end of the flow
     * @param invalidateBearerToken a bearer token from a previous session to invalidate
     * @return the URI for the authentication request
     */
    public URI buildAuthenticationRequest(final UUID relyingPartyId,
            final String redirectToUrl,
            @Nullable final String invalidateBearerToken) {
        //
        affirmNotNull(relyingPartyId, "invalid relying party id");
        affirmArgumentNotNullOrEmpty(redirectToUrl, "invalid redirect url");

        //FIXME: validate the redirectToUrl

        // If a bearer token was supplied, kill it.
        if(!isNullOrEmpty(invalidateBearerToken)) {
            credentialService.invalidate(invalidateBearerToken);
        }

        OpenIDConnectRelyingPartyCredential credential = findCredential(relyingPartyId).block();
        affirmNotNull(credential, "invalid credential specified");

        // Generate random state string for pairing the response to the request
        // State is also utilized as the OIDC "session identifier"
        State state = new State();
        // Generate a nonce
        Nonce nonce = new Nonce();

        // Persist the necessary state info for the callback.
        OpenIDConnectState openIDConnectState = new OpenIDConnectState() //
                .setState(state.getValue()) //
                .setRedirectUrl(redirectToUrl) //
                .setNonce(nonce.getValue()) //
                .setRelyingPartyId(relyingPartyId);
        stateGateway.persist(openIDConnectState).blockFirst();
        openIDConnectLogService.logEvent(state.getValue(), START).blockFirst();

        OIDCProviderMetadata providerMetadata = getProviderMetadata(credential, state.getValue());

        // The client identifier provisioned by the server
        ClientID clientID = new ClientID(credential.getClientId());

        // Convert the Scope values to a Scope object.
        Scope scope = Scope.parse(credential.getAuthenticationRequestScope());

        // Compose the request (in code flow)
        AuthenticationRequest authRequest;
        try {
            authRequest = new AuthenticationRequest(providerMetadata.getAuthorizationEndpointURI(), //
                                                    new ResponseType("code"), //
                                                    scope, //
                                                    clientID, //
                                                    getCallbackUri(), //
                                                    state, //
                                                    nonce);
        } catch (RuntimeException e) {
            openIDConnectLogService.logError(state.getValue(), "failure building authentication request.", e);
            throw e;
        }

        // get the URI to return, so that it can be logged.
        URI authenticationRequestRedirect = authRequest.toURI();
        // log the redirect; it contains heaps of the above information (scopes, clientID, callback url, ...)
        openIDConnectLogService.logEvent(state.getValue(), REDIRECT, authenticationRequestRedirect.toString()) //
                .blockFirst();

        return authenticationRequestRedirect;
    }

    /**
     * Get the URL that was part of the original request which the user should be redirected to
     *
     * @param state the state
     * @return the URL that the user should be redirected to
     */
    public String getContinueTo(final String state) {
        return stateGateway.find(state) //
                .map(OpenIDConnectState::getRedirectUrl) //
                .block();
    }

    /**
     * Acquire an account based on the response of the OIDC callback.
     *
     * @param queryParamCode the OIDC Identity Provider supplied code
     * @param queryParamState the state supplied during the authentication request
     * @return a short-lived bearer token which can be used to authenticate with
     */
    public WebSessionToken processCallback(final String queryParamCode, final String queryParamState) {

        affirmArgumentNotNullOrEmpty(queryParamCode, "invalid code");
        affirmArgumentNotNullOrEmpty(queryParamState, "invalid state");

        // load the previously stored state
        OpenIDConnectState openIDConnectState = stateGateway.find(queryParamState).block();
        affirmNotNull(openIDConnectState, "invalid state supplied");

        OpenIDConnectRelyingPartyCredential credential = findCredential(openIDConnectState.getRelyingPartyId()).block();
        affirmNotNull(credential, "invalid relyingPartyId");

        openIDConnectLogService.logEvent(openIDConnectState.getState(), PROCESS_CALLBACK).blockFirst();
        if (credential.isLogDebug()) {
            // (state is implicitly logged in the key)
            openIDConnectLogService.logEventSensitive(openIDConnectState.getState(), //
                                                      PROCESS_CALLBACK_PARAMETERS, //
                                                      "code: " + queryParamCode).blockFirst();
        }

        // process the incoming request
        OIDCTokenResponse successResponse = performTokenRequest(credential, queryParamCode, queryParamState);

        // Extract the tokens.
        OIDCTokens oidcTokens = successResponse.getOIDCTokens();
        //RefreshToken refreshToken = successResponse.getOIDCTokens().getRefreshToken();

        JWT idToken = oidcTokens.getIDToken();
        JWTClaimsSet claims;
        try {
            claims = idToken.getJWTClaimsSet();
            if (credential.isLogDebug()) {
                for (Map.Entry<String, Object> entry : claims.getClaims().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    openIDConnectLogService.logEventSensitive(openIDConnectState.getState(), JWT_CLAIM, //
                                                              String.format("%s: %s", key, value)) //
                            .blockFirst();
                }
            }
        } catch (java.text.ParseException e) {
            openIDConnectLogService.logError(openIDConnectState.getState(), "parsing claim", e).blockFirst();
            throw new OIDCTokenParseFault("Unable to parse idToken: " + e.getMessage());
        }

        // verify the nonce
        String suppliedNonce = null;
        try {
            suppliedNonce = claims.getStringClaim("nonce");
            affirmArgument(openIDConnectState.getNonce().equals(suppliedNonce), "nonce mismatch");
        } catch (java.text.ParseException e) {
            openIDConnectLogService.logError(openIDConnectState.getState(), "getting nonce", e).blockFirst();
            throw new OIDCTokenParseFault("Unable to parse nonce: " + e.getMessage());
        } catch (IllegalArgumentFault f) {
            openIDConnectLogService.logError(openIDConnectState.getState(), "nonce mismatch", f).blockFirst();
            throw f;
        }

        // (Optional) verify the "iss" claim matches the issuer
        // (Optional) verify the "azp" claim matches the CLIENT ID

        // FIXME: verify the token signature (does this enforce required arguments?)

        // extract relevant attributes.
        String jwtGivenName;
        String jwtFamilyName;
        String jwtEmail;
        Boolean jwtEmailVerified;
        String jwtSubject; // this is the non-changing unique ID from the identity provider.
        try {
            // PLT-5477 - OIDC should be able to create or provision an account with only a family or given name value (don't require both).
            jwtGivenName = claims.getStringClaim("given_name");
            jwtFamilyName = claims.getStringClaim("family_name");
            if (isNullOrEmpty(jwtGivenName) && isNullOrEmpty(jwtFamilyName)) {
                affirmArgumentNotNullOrEmpty((String) null, "missing required 'given_name' and/or 'family_name' claims");
            }

            //
            jwtEmail = Emails.normalize(claims.getStringClaim("email"));
            affirmArgumentNotNullOrEmpty(jwtEmail, "missing required 'email' claim");
            //
            jwtEmailVerified = claims.getBooleanClaim("email_verified");
            jwtEmailVerified = jwtEmailVerified == null ? Boolean.FALSE : jwtEmailVerified;
            //
            jwtSubject = claims.getStringClaim("sub");
            affirmArgumentNotNullOrEmpty(jwtSubject, "missing required 'sub' claim");
        } catch (java.text.ParseException e) {
            openIDConnectLogService.logError(openIDConnectState.getState(), "parsing claims", e).blockFirst();
            throw new OIDCTokenParseFault("Unable to parse claims: " + e.getMessage());
        } catch (IllegalArgumentFault f) {
            openIDConnectLogService.logError(openIDConnectState.getState(), "missing field", f).blockFirst();
            throw f;
        }

        Account account = getFederatedAccount(credential, openIDConnectState.getState(), //
                                              jwtGivenName, jwtFamilyName, jwtEmailVerified, jwtEmail, jwtSubject);

        updateAccountProperties(credential, account, jwtGivenName, jwtFamilyName, jwtEmailVerified, jwtEmail);
        recordNonDefaultClaims(credential, openIDConnectState, account, claims);

        // generate a bearer token
        WebSessionToken webSessionToken = credentialService.createWebSessionToken(account.getId(), //
                                                                                  credential.getSubscriptionId(), //
                                                                                  credential.getRelyingPartyId()) //
                .block();
        if (webSessionToken == null) {
            log.error("unable to create a websession token!");
            throw new WebTokenNotFoundFault("unable to proceed");
        }

        // record the token response tokens
        com.nimbusds.oauth2.sdk.token.AccessToken responseAccessToken = oidcTokens.getAccessToken();
        AccessToken accessToken = new AccessToken() //
                .setWebSessionToken(webSessionToken.getToken()) //
                .setId(UUIDs.timeBased()) //
                .setState(openIDConnectState.getState()) //
                .setRelyingPartyId(credential.getRelyingPartyId()) //
                .setAccessToken(responseAccessToken.getValue()) //
                .setTokenType(responseAccessToken.getType().getValue()) //
                .setExpiresIn(responseAccessToken.getLifetime());
        accessTokenByWebSessionTokenGateway.persist(accessToken).blockLast();

        // log & track success
        openIDConnectLogService.logEvent(openIDConnectState.getState(), SUCCESS).blockFirst();
        SessionAccount sa = new SessionAccount() //
                .setId(UUIDs.timeBased()) //
                .setAccountId(account.getId()) //
                .setSessionId(openIDConnectState.getState());
        sessionAccountGateway.persist(sa).blockFirst();

        // return the websession token.
        return webSessionToken;
    }

    /**
     * Logout of an existing session using a bearer token.
     *
     * @param bearerToken the bearerToken
     * @param source the original URL which was used to initiate the logout action, optional.
     * @return the URI to redirect the user logging to
     */
    public URI logout(final String bearerToken, final String source) {

        // Attempt to handle cases with no cookie supplied, with a cookie we ca not figure out where to redirect to.
        // example case: logout across 2 different tabs (first has cookie, second doesn't have shit)
        if (isNullOrEmpty(bearerToken) && !isNullOrEmpty(source)) {
            // as long as the source is a valid redirect url... we will redirect there.
            // Why? the hope is that by redirecting to the source, it will initiate a kick-back to the OIDC id provider.
            try {
                URI sourceUri = new URI(source);
                if (sourceUri.getHost().endsWith(".phx-spr.com")) {
                    return sourceUri;
                }
            } catch (URISyntaxException e) {
                // swallow, invalid source supplied.
            }
        }

        // no cookie & no valid source to try, ugly error.
        affirmArgumentNotNullOrEmpty(bearerToken, "missing required bearerToken");

        // invalidate the SPR previous token.
        credentialService.invalidate(bearerToken);

        // find the OIDC tokens
        AccessToken accessToken = accessTokenByWebSessionTokenGateway.find(bearerToken).block();
        if (accessToken == null) {
            // could not find the tokens, redirect out to the default place for safety.
            return failoverLogoutRedirect(null);
        }

        //
        UUID relyingPartyId = accessToken.getRelyingPartyId();
        String authenticateState = accessToken.getState();
        openIDConnectLogService.logEvent(authenticateState, START_LOGOUT).blockFirst();

        // get the credential so we can fetch the metadata
        OpenIDConnectRelyingPartyCredential credential = findCredential(relyingPartyId).block();
        if (credential == null) {
            // could not find the credential, redirect out to the default place for safety.
            openIDConnectLogService.logError(authenticateState,
                                             "could not find credential for relyingPartyId: " + relyingPartyId);
            return failoverLogoutRedirect(authenticateState);
        }

        // fetch the metadata
        OIDCProviderMetadata providerMetadata = getProviderMetadata(credential, authenticateState);

        // if we can, revoke the token.
        if (providerMetadata.getRevocationEndpointURI() != null) {
            // The credentials to authenticate the client at the token endpoint
            ClientID clientID = new ClientID(credential.getClientId());
            Secret clientSecret = new Secret(credential.getClientSecret());
            ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

            // Rebuild the Access token
            com.nimbusds.oauth2.sdk.token.AccessToken oidcToken;
            oidcToken = new com.nimbusds.oauth2.sdk.token.BearerAccessToken(accessToken.getAccessToken());

            TokenRevocationRequest tokenRevocationRequest;
            tokenRevocationRequest = new TokenRevocationRequest(providerMetadata.getRevocationEndpointURI(), //
                                                                clientAuth, //
                                                                oidcToken);

            HTTPRequest tokenRevokeReq = tokenRevocationRequest.toHTTPRequest();
            if (credential.isLogDebug()) {
                String requestAsString = httpRequestAsString(tokenRevokeReq);
                openIDConnectLogService.logEvent(authenticateState, TOKEN_REVOKE_REQUEST, requestAsString).blockFirst();
            }

            try {
                // should this be wrapped in the client lib somehow?
                // per spec: https://tools.ietf.org/html/rfc7009 -- errors will never really be returned.
                HTTPResponse tokenRevokeResponse = tokenRevokeReq.send();
                //
                String responseAsString = httpResponseAsString(tokenRevokeResponse);
                openIDConnectLogService.logEvent(authenticateState, TOKEN_REVOKE_RESPONSE, responseAsString) //
                        .blockFirst();
                if (!tokenRevokeResponse.indicatesSuccess()) {
                    String msg = String.format("status code: %s reason: %s", //
                                               tokenRevokeResponse.getStatusCode(), //
                                               tokenRevokeResponse.getContent());
                    openIDConnectLogService.logEvent(authenticateState, ERROR, msg) //
                            .blockFirst();
                }
            } catch (IOException e) {
                openIDConnectLogService.logError(authenticateState, "failure in token revoke", e).blockFirst();
                throw new NotFoundFault("token endpoint not found");
            }
        }

        // try to redirect to the end session endpoint as noted in the metadata.
        URI metadataEndSessionEndpointURI = providerMetadata.getEndSessionEndpointURI();
        if (metadataEndSessionEndpointURI == null) {
            // not specified, use failover.
            return failoverLogoutRedirect(authenticateState);
        }

        openIDConnectLogService.logEvent(authenticateState, REDIRECT, metadataEndSessionEndpointURI.toString()) //
                .blockFirst();
        return metadataEndSessionEndpointURI;
    }

    URI failoverLogoutRedirect(String state) {
        URI failoverUri = URI.create("https://www.pearson.com");
        if (state != null) {
            openIDConnectLogService.logEvent(state, REDIRECT, failoverUri.toString()).blockFirst();
        }
        return failoverUri;
    }

    OIDCTokenResponse performTokenRequest(final OpenIDConnectRelyingPartyCredential credential,
            final String queryParamCode,
            final String state) {
        //
        OIDCProviderMetadata providerMetadata = getProviderMetadata(credential, state);

        // Construct the code grant from the code obtained from the authz endpoint
        // and the original callback URI used at the authz endpoint
        AuthorizationCode code = new AuthorizationCode(queryParamCode);
        AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, getCallbackUri());

        // The credentials to authenticate the client at the token endpoint
        ClientID clientID = new ClientID(credential.getClientId());
        Secret clientSecret = new Secret(credential.getClientSecret());
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

        // Make the token request
        TokenRequest request = new TokenRequest(providerMetadata.getTokenEndpointURI(), clientAuth, codeGrant);
        HTTPRequest tokenHttpRequest = request.toHTTPRequest();
        if (credential.isLogDebug()) {
            String requestAsString = httpRequestAsString(tokenHttpRequest);
            openIDConnectLogService.logEventSensitive(state, TOKEN_REQUEST, requestAsString).blockFirst();
        }

        TokenResponse tokenResponse;
        try {
            tokenResponse = OIDCTokenResponseParser.parse(tokenHttpRequest.send());
            if (credential.isLogDebug()) {
                HTTPResponse response = tokenResponse.toHTTPResponse();
                String responseAsString = httpResponseAsString(response);
                openIDConnectLogService.logEventSensitive(state, TOKEN_RESPONSE, responseAsString).blockFirst();
            }
        } catch (ParseException | IOException e) {
            openIDConnectLogService.logError(state, "failure in token exchange", e).blockFirst();
            throw new OIDCTokenParseFault("Unable to receive or parse OIDCToken response: " + e.getMessage());
        }

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            String err = errorResponse.toJSONObject().toJSONString();
            openIDConnectLogService.logEvent(state, TOKEN_RESPONSE_FAIL, err).blockFirst();
            throw new OIDCTokenFault(err);
        }

        OIDCTokenResponse successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        openIDConnectLogService.logEvent(state, TOKEN_RESPONSE_OK).blockFirst();
        if (credential.isLogDebug()) {
            String tokens = ((OIDCTokenResponse) tokenResponse).toJSONObject().toJSONString();
            openIDConnectLogService.logEventSensitive(state, TOKEN_RESPONSE_OK_RESULT, tokens).blockFirst();
        }
        return successResponse;
    }

    /**
     * using the federation api, try to find the account. If account is not found, provision new account
     */
    Account getFederatedAccount(final OpenIDConnectRelyingPartyCredential credential,
            final String state,
            @Nullable final String jwtGivenName,
            @Nullable final String jwtFamilyName,
            final Boolean jwtEmailVerified,
            final String jwtEmail,
            final String jwtSubject) {

        Account account = accountService.findByFederation(credential.getSubscriptionId(), //
                                                          credential.getClientId(), //
                                                          jwtSubject).block();

        // FIXME: security issues;
        //  - ability to gain access to another account that is not yours by using a self-setup IDP.
        //  - ???

        // federation link not found? try using the email address
        if (account == null) {
            account = accountService.findByEmail(jwtEmail).blockFirst();
            if (account == null) {
                //  - not found: provision as a student

                // ensure that new accounts have a verified email address.
                if (credential.isEnforceVerifiedEmail() && !jwtEmailVerified) {
                    throw new IllegalArgumentFault("email address must be verified for account provision");
                }
                AccountAdapter adapter = accountService.provision(AccountProvisionSource.OIDC, //
                                                                  credential.getSubscriptionId(), //
                                                                  ImmutableSet.of(AccountRole.STUDENT), //
                                                                  null, jwtGivenName, jwtFamilyName, null, //
                                                                  jwtEmail, //
                                                                  null, //
                                                                  null, //
                                                                  null, //
                                                                  null,
                                                                  AuthenticationType.OIDC);
                account = adapter.getAccount();
                openIDConnectLogService.logEvent(state, ACCOUNT_PROVISIONED).blockFirst();
            } else {
                openIDConnectLogService.logEvent(state, ACCOUNT_LOCATED_BY_EMAIL).blockFirst();
            }
            //  - found: associate the account with the federation.
            accountService.addFederation(credential.getSubscriptionId(), //
                                         credential.getClientId(), //
                                         jwtSubject, //
                                         account.getId()).block();
        } else {
            openIDConnectLogService.logEvent(state, ACCOUNT_LOCATED_BY_FEDERATION).blockFirst();
        }
        return account;
    }

    /**
     * Update givenName, familyName, verifiedEmail if they are different from values in DB
     */
    void updateAccountProperties(final OpenIDConnectRelyingPartyCredential credential, final Account account,
            final String jwtGivenName,
            final String jwtFamilyName,
            final Boolean jwtEmailVerified,
            final String jwtEmail) {
        // update the account properties. (don't remove roles)
        AccountIdentityAttributes identityAttributes = accountService.findIdentityByAccount(account).blockFirst();
        if (identityAttributes == null) {
            // there are no identity attributes, so add some (if possible).
            if (jwtGivenName != null && jwtFamilyName != null) {
                accountService.setIdentityNames(account.getId(), null, jwtGivenName, jwtFamilyName, null);
            }
            if (!credential.isEnforceVerifiedEmail() || jwtEmailVerified) {
                accountService.addVerifiedEmail(account.getId(), jwtEmail);
            }
        } else {
            // check to see if we can update any.
            if (!jwtGivenName.equals(identityAttributes.getGivenName()) //
                    || !jwtFamilyName.equals(identityAttributes.getFamilyName())) {
                // update given/family names.
                accountService.setIdentityNames(identityAttributes.getAccountId(), //
                                                identityAttributes.getHonorificPrefix(), //
                                                jwtGivenName, jwtFamilyName, //
                                                identityAttributes.getHonorificSuffix());
            }
            if ((!credential.isEnforceVerifiedEmail() || jwtEmailVerified) && !identityAttributes.getEmail().contains(jwtEmail)) {
                // update only verified emails.
                accountService.addVerifiedEmail(identityAttributes.getAccountId(), jwtEmail);
            }
        }
    }

    void recordNonDefaultClaims(final OpenIDConnectRelyingPartyCredential credential,
            final OpenIDConnectState state,
            final Account account,
            final JWTClaimsSet claims) {

        List<Flux<Void>> work = new ArrayList<>();

        // walk the supplied claims
        for (Map.Entry<String, Object> entry : claims.getClaims().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue() == null ? null : entry.getValue().toString();
            // Don't record default properties.
            if (IGNORE_IAM_CLAIMS.contains(name)) {
                continue;
            }

            // record the data.
            work.add(claimService.add(account.getId(), credential.getSubscriptionId(), name, value));
            // log the event.
            work.add(openIDConnectLogService.logEvent(state.getState(), JWT_CLAIM, "Recorded profile claim: " + name));
            // perform debug logging.
            if (credential.isLogDebug()) {
                final String msg = String.format("Recorded profile claim: %s = %s", name, value);
                work.add(openIDConnectLogService.logEventSensitive(state.getState(), JWT_CLAIM, msg));
            }
        }

        // exec the work.
        Flux.fromIterable(work).flatMap(x -> x).blockLast();
    }

    OIDCProviderMetadata getProviderMetadata(OpenIDConnectRelyingPartyCredential credential, String sessionId) {
        try {
            OIDCProviderMetadata providerMetadata = discoveryDocumentCache.get(credential.getIssuerUrl());
            //
            if (credential.isLogDebug()) {
                String wellKnownUrl = format("%s/.well-known/openid-configuration",
                                             providerMetadata.getIssuer().getValue());
                openIDConnectLogService.logEvent(sessionId, RETRIEVE_METADATA, wellKnownUrl) //
                        .blockFirst();

                String metadata = providerMetadata.toString();
                openIDConnectLogService.logEventSensitive(sessionId, RETRIEVE_METADATA_RESULT, metadata) //
                        .blockFirst();
            }

            return providerMetadata;
        } catch (RuntimeException e) {
            //
            openIDConnectLogService.logError(sessionId, "failure loading provider metadata.", e) //
                    .blockFirst();
            // rethrow.
            throw e;
        }
    }

    String httpRequestAsString(HTTPRequest request) {
        StringBuilder b = new StringBuilder();
        b.append(request.getMethod()).append(" ").append(request.getURL().getPath()).append("\n");
        b.append("Host: ").append(request.getURL().getHost()).append("\n");
        appendHttpHeadersToString(b, request.getHeaderMap());
        // format for easier reading.
        b.append(request.getQuery().replaceAll("&", "&\n"));

        return b.toString();
    }

    String httpResponseAsString(HTTPResponse response) {
        StringBuilder b = new StringBuilder();
        b.append("HTTP ").append(response.getStatusCode()).append("\n");
        appendHttpHeadersToString(b, response.getHeaderMap());
        b.append(response.getContent());

        return b.toString();
    }

    private void appendHttpHeadersToString(StringBuilder b, Map<String, List<String>> headerMap) {
        for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            for (String headerValue : entry.getValue()) {
                b.append(entry.getKey()).append(": ").append(headerValue).append("\n");
            }
        }
        b.append("\n");
    }
}
