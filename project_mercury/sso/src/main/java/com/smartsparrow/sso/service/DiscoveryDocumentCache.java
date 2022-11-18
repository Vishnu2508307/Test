package com.smartsparrow.sso.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.smartsparrow.sso.lang.OIDCProviderMetadataFault;

/**
 * An OIDC discovery document lives on the Identity Provider's server. It makes sense that this information should be
 * cached for a period of time.
 */
public class DiscoveryDocumentCache {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryDocumentCache.class);

    //
    private static long CACHE_DURATION = 15;
    private static TimeUnit CACHE_TIME_UNIT = TimeUnit.MINUTES;

    // TODO: replace this local instance cache with Redis.
    private static LoadingCache<String, OIDCProviderMetadata> cache;

    static {
        // create the loader.
        CacheLoader<String, OIDCProviderMetadata> loader;
        loader = new CacheLoader<String, OIDCProviderMetadata>() {
            @Override
            public OIDCProviderMetadata load(String key) {
                return getProviderMetadata(key);
            }
        };
        // a removal listener for logging; removals occur on the READ operation (not a thread)
        RemovalListener<String, OIDCProviderMetadata> removalListener = n -> {
            if (n.wasEvicted()) {
                String cause = n.getCause().name();
                if (log.isDebugEnabled()) {
                    log.debug("removed item from document discovery cache key:{} cause:{}", n.getKey(), cause);
                }
            }
        };
        // build the cache.
        cache = CacheBuilder.newBuilder() //
                .expireAfterWrite(CACHE_DURATION, CACHE_TIME_UNIT) //
                .removalListener(removalListener) //
                .maximumSize(256) //
                .build(loader);
    }

    /**
     * Get the Identity Provider Metadata
     *
     * If the metadata is at:
     *   https://accounts.google.com/.well-known/openid-configuration
     * then the Issuer is at:
     *   https://accounts.google.com
     *
     * @param oidcProviderIssuer the URL of the issuer.
     *
     * @return the OIDC provider metadata information
     */
    public OIDCProviderMetadata get(final String oidcProviderIssuer) {
        try {
            return cache.get(oidcProviderIssuer);
        } catch (ExecutionException e) {
            log.error("a checked exception was thrown from the underlying loader", e);
            throw new OIDCProviderMetadataFault("[1] Unable to get provider metadata: " + e.getMessage());
        }
    }

    private static OIDCProviderMetadata getProviderMetadata(final String oidcProviderIssuer) {
        //
        Issuer issuer = new Issuer(oidcProviderIssuer);

        // Will resolve the OpenID provider metadata automatically
        OIDCProviderConfigurationRequest request = new OIDCProviderConfigurationRequest(issuer);

        // Make HTTP request
        HTTPRequest httpRequest = request.toHTTPRequest();
        HTTPResponse httpResponse;
        try {
            log.debug("requesting for provider metadata from {}", httpRequest.getURI());
            httpResponse = httpRequest.send();
        } catch (IOException e) {
            throw new OIDCProviderMetadataFault("[2] Unable to get provider metadata: " + e.getMessage());
        }

        // Parse OpenID provider metadata
        OIDCProviderMetadata opMetadata;
        try {
            opMetadata = OIDCProviderMetadata.parse(httpResponse.getContentAsJSONObject());
        } catch (ParseException e) {
            throw new OIDCProviderMetadataFault("Unable to parse provider metadata: " + e.getMessage());
        }

        return opMetadata;
    }
}
