package com.smartsparrow.asset.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.BronteAssetContext;

import reactor.core.publisher.Mono;

@Singleton
public class BronteAssetTypeHandler {

    private Map<AssetMediaType, Provider<BronteAssetType<? extends BronteAssetResponse>>> assetTypeConsumers;

    @Inject
    public BronteAssetTypeHandler(Map<AssetMediaType, Provider<BronteAssetType<? extends BronteAssetResponse>>> assetTypeConsumers) {
        this.assetTypeConsumers = assetTypeConsumers;
    }

    /**
     * invoke handler based on asset media type provider.
     * @param assetSource the asset source
     * @param assetUrn the asset urn
     * @param metadata the metadata info of an asset
     * @param assetMediaType the asset media type
     * @return mono of asset result
     */
    public Mono<BronteAssetResponse> handle(final AssetSource assetSource,
                                            final String assetUrn,
                                            final Map<String, String> metadata,
                                            final AssetMediaType assetMediaType) {

        affirmArgument(assetSource != null, "assetSource is required");
        affirmArgument(assetMediaType != null, "assetMediaType is required");
        BronteAssetContext bronteAssetContext = new BronteAssetContext()
                .setAssetSource(assetSource)
                .setAssetUrn(assetUrn)
                .setMetadata(metadata)
                .setAssetMediaType(assetMediaType);

        return Mono.just(bronteAssetContext)
                .flatMap(context ->
                                 ((BronteAssetType<? extends BronteAssetResponse>) assetTypeConsumers.get(
                                         assetMediaType).get()).handle(context));
    }
}
