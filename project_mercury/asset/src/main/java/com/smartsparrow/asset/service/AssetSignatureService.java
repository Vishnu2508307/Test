package com.smartsparrow.asset.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmDoesNotThrow;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.akamai.edgeauth.EdgeAuth;
import com.akamai.edgeauth.EdgeAuthBuilder;
import com.akamai.edgeauth.EdgeAuthException;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AkamaiTokenAuthenticationConfiguration;
import com.smartsparrow.asset.data.AssetSignature;
import com.smartsparrow.asset.data.AssetSignatureConfiguration;
import com.smartsparrow.asset.data.AssetSignatureGateway;
import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AssetSignatureService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetSignatureService.class);

    private final AssetSignatureGateway assetSignatureGateway;
    private final AssetSignatureConfigurationDeserializer assetSignatureConfigurationDeserializer;

    @Inject
    public AssetSignatureService(final AssetSignatureGateway assetSignatureGateway,
                                 final AssetSignatureConfigurationDeserializer assetSignatureConfigurationDeserializer) {
        this.assetSignatureGateway = assetSignatureGateway;
        this.assetSignatureConfigurationDeserializer = assetSignatureConfigurationDeserializer;
    }

    /**
     * Create the asset signature configuration for an host at a specific path
     *
     * @param host the url host
     * @param path the url path
     * @param config a stringify json object representing the configurations
     * @param type the type of signature
     * @return a mono with the created asset signature
     * @throws IllegalArgumentFault when any required argument is missing or invalid
     */
    @Trace(async = true)
    public Mono<AssetSignature> create(final String host, final String path, final String config,
                                       final AssetSignatureStrategyType type) {
        affirmArgumentNotNullOrEmpty(host, "host is required");
        affirmArgument(path != null, "path is required");
        affirmArgumentNotNullOrEmpty(config, "config is required");
        affirmArgument(type != null, "assetSignatureType is required");

        // check that the config is valid
        affirmDoesNotThrow(() -> assetSignatureConfigurationDeserializer.deserialize(type, config),
                new IllegalArgumentFault(String.format("invalid configuration for %s", type)));

        // create the assetSignature
        final AssetSignature assetSignature = new AssetSignature()
                .setConfig(config)
                .setHost(host)
                .setPath(path)
                .setId(UUIDs.timeBased())
                .setAssetSignatureStrategyType(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION);

        // persist the signature
        return assetSignatureGateway.persist(assetSignature)
                .singleOrEmpty()
                .thenReturn(assetSignature)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the asset signature configurations for a specific host and path
     *
     * @param host the url host to delete the config for
     * @param path the url path to delete the config for
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final String host, final String path) {
        affirmArgumentNotNullOrEmpty(host, "host is required");
        affirmArgument(path != null, "path is required");

        return assetSignatureGateway.delete(new AssetSignature()
                .setHost(host)
                .setPath(path))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Check if the url domain is configured with a signature strategy and proceeds to sign the url. If no strategy is
     * found the url is returned as is
     *
     * @param url the url to sign
     * @return a mono containing the signed url or the original non signed url when configs not found
     */
    @Trace(async = true)
    public Mono<String> signUrl(final String url) {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost();

            // hardcode the path to an empty string for now
            return assetSignatureGateway.findAssetSignature(host, "")
                    .flatMap(assetSignature -> {
                        // get the strategy
                        final AssetSignatureStrategyType type = assetSignature.getAssetSignatureStrategyType();
                        // deserialize the configuration
                        final AssetSignatureConfiguration deserialized = assetSignatureConfigurationDeserializer
                                .deserialize(type, assetSignature.getConfig());
                        // sign the url
                        return sign(url, deserialized, type);
                    })
                    // when no configs are found, just return the non signed url
                    .defaultIfEmpty(url)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } catch (URISyntaxException e) {
            throw new IllegalStateFault("invalid url");
        }
    }

    /**
     * Calls the appropriate signing strategy implementation according to the signature strategy type
     *
     * @param url the url to sign
     * @param configuration the asset signature configurations
     * @param type the signature strategy type
     * @return a mono with the signed url
     */
    @Trace(async = true)
    private Mono<String> sign(final String url, final AssetSignatureConfiguration configuration,
                              final AssetSignatureStrategyType type) {

        if (type.equals(AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION)) {
            return sign(url, (AkamaiTokenAuthenticationConfiguration) configuration)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }
        // no signature required, return as is
        return Mono.just(url).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Sign the url using the akamai token authentication method
     *
     * @param url the url to sign
     * @param configuration the akamai token authentication configuration
     * @return a mono with the signed url
     */
    @Trace(async = true)
    private Mono<String> sign(final String url, final AkamaiTokenAuthenticationConfiguration configuration) {
        try {
            final URI uri = new URI(url);
            // set required key and tokenName params
            EdgeAuthBuilder edgeAuthBuilder = new EdgeAuthBuilder()
                    .escapeEarly(true)
                    .key(configuration.getKey())
                    .tokenName(configuration.getTokenName());

            //
            // set all other params when defined
            //

            if (configuration.getAlgorithm() != null) {
                edgeAuthBuilder.algorithm(configuration.getAlgorithm());
            }

            if (configuration.getIp() != null) {
                edgeAuthBuilder.ip(configuration.getIp());
            }

            if (configuration.getStartTime() != null) {
                edgeAuthBuilder.startTime(configuration.getStartTime());
            }

            if (configuration.getEndTime() != null) {
                edgeAuthBuilder.endTime(configuration.getEndTime());
            }

            if (configuration.getWindowSeconds() != null) {
                edgeAuthBuilder.windowSeconds(configuration.getWindowSeconds());
            }

            // build auth and sign the url
            final EdgeAuth edgeAuth = edgeAuthBuilder.build();
            final String token = edgeAuth.generateURLToken(uri.getPath());
            return Mono.just(String.format("%s?%s=%s", uri, edgeAuth.getTokenName(), token))
                    .doOnEach(ReactiveTransaction.linkOnNext());
        } catch (EdgeAuthException | URISyntaxException e) {
            log.error("error ", e);
            throw new IllegalStateFault("failed to sign asset");
        }
    }
}
