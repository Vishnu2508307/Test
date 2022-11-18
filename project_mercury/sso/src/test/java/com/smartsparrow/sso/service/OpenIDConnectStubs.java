package com.smartsparrow.sso.service;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.smartsparrow.sso.wiring.OpenIDConnectConfig;

public class OpenIDConnectStubs {

    public static final String ISSUER = "https://accounts.google.com";
    public static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String CALLBACK_URL = "https://localhost:8080/callback";
    public static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    public static DiscoveryDocumentCache mockDiscoveryDocumentCache(DiscoveryDocumentCache cache) throws URISyntaxException {
        OIDCProviderMetadata metadata = mock(OIDCProviderMetadata.class);
        when(metadata.getAuthorizationEndpointURI()).thenReturn(URI.create(AUTH_ENDPOINT));
        when(metadata.getTokenEndpointURI()).thenReturn(URI.create(TOKEN_ENDPOINT));
        when(cache.get(ISSUER)).thenReturn(metadata);
        return cache;
    }

    public static OpenIDConnectConfig mockOpenIDConnectConfig(OpenIDConnectConfig config) {
        when(config.getCallbackUrl()).thenReturn(CALLBACK_URL);
        return config;
    }
}
