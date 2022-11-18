package com.smartsparrow.asset.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class ExternalAssetService implements AssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExternalAssetService.class);

    private final AssetGateway assetGateway;
    private final AssetBuilder assetBuilder;
    private final AssetSignatureService assetSignatureService;

    @Inject
    public ExternalAssetService(final AssetGateway assetGateway,
                                final AssetBuilder assetBuilder,
                                final AssetSignatureService assetSignatureService) {
        this.assetGateway = assetGateway;
        this.assetBuilder = assetBuilder;
        this.assetSignatureService = assetSignatureService;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getAssetPayload(final String urn) {
        affirmArgumentNotNullOrEmpty(urn, "urn is required");
        // try fetching the asset id for this urn
        return assetGateway.findAssetId(urn)
                .map(AssetIdByUrn::getAssetId)
                // get the asset payload
                .flatMap(this::getAssetPayload)
                // log a line if anything goes wrong, thank you
                .doOnEach(log.reactiveErrorThrowable("failed to get asset payload", throwable -> new HashMap<String, Object>(){
                    {put("assetUrn", urn);}
                }));
    }
    @Trace(async = true)
    public Mono<AssetPayload> getAssetPayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        Mono<AssetSummary> summary = assetGateway.fetchAssetById(assetId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<Map<String, String>> metadata = assetGateway.fetchMetadata(assetId)
                .collectMap(AssetMetadata::getKey, AssetMetadata::getValue)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<Map<String, Object>> sources = summary.flatMap(asset -> {
            affirmArgument(asset.getProvider().equals(AssetProvider.EXTERNAL),
                    String.format("asset provider %s not supported in BronteAssetService", asset.getProvider()));
            return getExternalSource(assetId);
        });

        return Mono.zip(summary, metadata, sources).map(tuple3 -> new AssetPayload()
                .setUrn(tuple3.getT1().getUrn())
                .setAsset(assetBuilder.setAssetSummary(tuple3.getT1())
                        .build(tuple3.getT1().getProvider())) //
                .putAllMetadata(tuple3.getT2())
                .putAllSources(tuple3.getT3()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return the external source for the asset as a map
     *
     * @param assetId the asset id to get the source for
     * @return a mono map of the external source payload or an empty stream when the external source is not found
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getExternalSource(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");

        return assetGateway.fetchExternalSource(assetId)
                .flatMap(externalSource -> buildPublicUrl(externalSource.getUrl())
                        .map(signedUrl -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("url", signedUrl);
                            return map;
                        })).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Build and sign the public url when required.
     *
     * @param relativeUrl asset source url
     * @return the full url to asset source
     */
    public Mono<String> buildPublicUrl(final String relativeUrl) {
        return assetSignatureService.signUrl(relativeUrl);
    }
}
