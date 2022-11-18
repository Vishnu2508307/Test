package com.smartsparrow.asset.service;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.BronteAssetContext;

import reactor.core.publisher.Mono;

public class BronteAssetEmptyTypeHandler implements BronteAssetType<BronteAssetEmptyResponse> {

    /**
     * This method handle asset types video, audio, document for now.
     * @param context the bronte asset info
     * @return mono of bronte asset empty result
     */
    @Trace(async = true)
    @Override
    public Mono<BronteAssetEmptyResponse> handle(final BronteAssetContext context) {
        return Mono.just(new BronteAssetEmptyResponse()
                                 .setAssetSource(context.getAssetSource())
                                 .setAssetMediaType(context.getAssetMediaType()));
    }
}
