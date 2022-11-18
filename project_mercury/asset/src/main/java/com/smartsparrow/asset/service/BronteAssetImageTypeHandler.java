package com.smartsparrow.asset.service;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.BronteAssetContext;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class BronteAssetImageTypeHandler implements BronteAssetType<BronteAssetImageResponse> {

    private final BronteImageAssetOptimizer bronteImageAssetOptimizer;

    @Inject
    public BronteAssetImageTypeHandler(final BronteImageAssetOptimizer bronteImageAssetOptimizer) {
        this.bronteImageAssetOptimizer = bronteImageAssetOptimizer;
    }

    /**
     * This method perform the optimization of an image type asset source
     * @param context the bronte asset info
     * @return mono of bronte asset image result
     */
    @Trace(async = true)
    @Override
    public Mono<BronteAssetImageResponse> handle(final BronteAssetContext context) {
        AssetSource assetSource = context.getAssetSource();
        // this is a safe cast
        return bronteImageAssetOptimizer.optimize((ImageSource) assetSource)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(assetId -> new BronteAssetImageResponse()
                        .setAssetId(assetId)
                        .setAssetSource(context.getAssetSource())
                        .setAssetMediaType(context.getAssetMediaType()));
    }
}
