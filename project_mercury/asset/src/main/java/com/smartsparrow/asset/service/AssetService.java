package com.smartsparrow.asset.service;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface AssetService {

    /**
     * Resolve a urn to an asset payload
     *
     * @param urn the urn to resolve the asset for
     * @return a mono of assetPayload
     */
    Mono<AssetPayload> getAssetPayload(final String urn);

    /**
     * Get the payload for an assetId
     *
     * @param assetId the id of the asset to get the payload for
     * @return a mono of assetPayload
     */
    Mono<AssetPayload> getAssetPayload(final UUID assetId);
}
