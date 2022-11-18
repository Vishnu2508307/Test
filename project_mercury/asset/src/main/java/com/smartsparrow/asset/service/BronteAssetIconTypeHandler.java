package com.smartsparrow.asset.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Map;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetConstant;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.BronteAssetContext;
import com.smartsparrow.asset.data.IconsByLibrary;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class BronteAssetIconTypeHandler implements BronteAssetType<BronteAssetIconResponse> {

    private final AssetGateway assetGateway;

    @Inject
    public BronteAssetIconTypeHandler(final AssetGateway assetGateway) {
        this.assetGateway = assetGateway;
    }

    /**
     * This method save icon images based on the metadata info.
     * @param context the bronte asset info
     * @return mono of bronte asset icon result
     */
    @Trace(async = true)
    @Override
    public Mono<BronteAssetIconResponse> handle(final BronteAssetContext context) {
        String assetUrn = context.getAssetUrn();
        Map<String, String> metaData = context.getMetadata();
        String iconLibrary = metaData.get(AssetConstant.ICON_LIBRARY);

        affirmArgument(assetUrn != null, "assetUrn is required");
        affirmArgument(iconLibrary != null, "key iconLibrary is missing in the metadata");

        IconsByLibrary iconsByLibrary = new IconsByLibrary()
                .setIconLibrary(iconLibrary)
                .setAssetUrn(assetUrn);

        return assetGateway.persist(iconsByLibrary)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(new BronteAssetIconResponse()
                                        .setAssetUrn(assetUrn)
                                        .setAssetSource(context.getAssetSource())
                                        .setAssetMediaType(context.getAssetMediaType())));
    }
}
