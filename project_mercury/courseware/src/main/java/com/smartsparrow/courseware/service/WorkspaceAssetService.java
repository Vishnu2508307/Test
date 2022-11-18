package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WorkspaceAssetService implements AssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(WorkspaceAssetService.class);

    private final AssetService bronteAssetService;
    private final AssetService alfrescoAssetService;
    private final AssetService externalAssetService;
    private final AssetGateway assetGateway;

    @Inject
    public WorkspaceAssetService(final @Named("BronteAssetService") AssetService bronteAssetService,
                                 final @Named("AlfrescoAssetService") AssetService alfrescoAssetService,
                                 final @Named("ExternalAssetService") AssetService externalAssetService,
                                 final AssetGateway assetGateway) {
        this.bronteAssetService = bronteAssetService;
        this.alfrescoAssetService = alfrescoAssetService;
        this.externalAssetService = externalAssetService;
        this.assetGateway = assetGateway;
    }

    /**
     * Get the asset payload by urn. Find the latest asset id associated to the urn and then invokes the
     * {@link WorkspaceAssetService#getAssetPayload(UUID)}.
     *
     * @param urn the urn to resolve the asset for
     * @return a mono of assetPayload
     */
    @Trace(async = true)
    public Mono<AssetPayload> getAssetPayload(final String urn) {
        // try fetching the asset id by urn
        return assetGateway.findAssetId(urn)
                .map(AssetIdByUrn::getAssetId)
                // get the asset payload
                .flatMap(this::getAssetPayload)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // log a line if anything goes wrong, thank you
                .doOnEach(log.reactiveErrorThrowable("failed to get asset payload", throwable -> new HashMap<String, Object>(){
                    {put("assetUrn", urn);}
                }));
    }

    /**
     * Get the asset payload for an assetId. Uses a strategy predicated on the asset provider to determine which
     * asset service implementation to invoke to get the payload
     *
     * @param assetId the id of the asset to get the payload for
     * @return a mono of asset payload
     */
    @Trace(async = true)
    public Mono<AssetPayload> getAssetPayload(final UUID assetId) {
        // fetch the summary by asset id
        return assetGateway.fetchAssetById(assetId)
                // use the assetProvider field from the summary to drive impl behaviour
                .flatMap(summary -> {
                    switch (summary.getProvider()) {
                        case AERO:
                            return bronteAssetService.getAssetPayload(assetId);
                        case EXTERNAL:
                            return externalAssetService.getAssetPayload(assetId);
                        case ALFRESCO:
                            return alfrescoAssetService.getAssetPayload(assetId);
                        default:
                            return Mono.error(new IllegalStateFault("Invalid asset provider"));
                    }
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return asset payload for list of asset urns.
     * @param assetUrns list of asser urns
     * @return flux of asset payload
     */
    @Trace(async = true)
    public Flux<AssetPayload> getAssetPayload(final List<String> assetUrns) {
        return assetUrns.stream()
                .map(assetUrn -> getAssetPayload(assetUrn)
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .flux())
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty());
    }

    /**
     * Update asset metadata
     * @param assetUrn the asset urn
     * @param key  metadata key
     * @param value metadata value
     * @return mono of asset metadata
     */
    @Trace(async = true)
    public Mono<AssetMetadata> updateAssetMetadata(final String assetUrn,
                                                   final String key,
                                                   final String value) {
        affirmArgument(assetUrn != null, "assetUrn is required");
        affirmArgument(key != null, "metadata key is missing");

        return assetGateway.findAssetId(assetUrn)
                .flatMap(assetIdByUrn -> {
                    AssetMetadata assetMetadata = new AssetMetadata()
                            .setAssetId(assetIdByUrn.getAssetId())
                            .setKey(key)
                            .setValue(value);
                    return assetGateway.persist(assetMetadata)
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .then(Mono.just(assetMetadata));
                });
    }
}
