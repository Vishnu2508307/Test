package com.smartsparrow.asset.service;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.BronteAssetContext;

import reactor.core.publisher.Mono;

public interface BronteAssetType<T extends BronteAssetResponse> {

    /**
     * Call handler based on the invoked asset media types.
     * @param context the bronte asset info
     * @return mono with asset result
     */
    @Trace(async = true)
    Mono<T> handle(BronteAssetContext context);
}
