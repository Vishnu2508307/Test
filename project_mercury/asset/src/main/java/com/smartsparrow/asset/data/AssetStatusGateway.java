package com.smartsparrow.asset.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AssetStatusGateway {

    private final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetStatusGateway.class);

    private final Session session;

    private final AssetStatusByUrnIdMutator assetStatusByUrnIdMutator;
    private final AssetStatusByUrnIdMaterializer assetStatusByUrnIdMaterializer;
    private final AssetStatusByUrnMutator assetStatusByUrnMutator;
    private final AssetStatusByUrnMaterializer assetStatusByUrnMaterializer;
    private final AssetStatusErrorByUrnIdMutator assetStatusErrorByUrnIdMutator;
    private final AssetStatusErrorByUrnIdMaterializer assetStatusErrorByUrnIdMaterializer;


    @Inject
    public AssetStatusGateway(final Session session,
                              final AssetStatusByUrnIdMutator assetStatusByUrnIdMutator,
                              final AssetStatusByUrnIdMaterializer assetStatusByUrnIdMaterializer,
                              final AssetStatusByUrnMutator assetStatusByUrnMutator,
                              final AssetStatusByUrnMaterializer assetStatusByUrnMaterializer,
                              final AssetStatusErrorByUrnIdMutator assetStatusErrorByUrnIdMutator,
                              final AssetStatusErrorByUrnIdMaterializer assetStatusErrorByUrnIdMaterializer) {
        this.session = session;
        this.assetStatusByUrnIdMutator = assetStatusByUrnIdMutator;
        this.assetStatusByUrnIdMaterializer = assetStatusByUrnIdMaterializer;
        this.assetStatusByUrnMutator = assetStatusByUrnMutator;
        this.assetStatusByUrnMaterializer = assetStatusByUrnMaterializer;
        this.assetStatusErrorByUrnIdMutator = assetStatusErrorByUrnIdMutator;
        this.assetStatusErrorByUrnIdMaterializer = assetStatusErrorByUrnIdMaterializer;
    }

    /**
     * Persist asset status information
     *
     * @param assetStatusByUrn, the asset urn object
     */
    @Trace(async = true)
    public Mono<Void> persistAssetStatusByUrn(AssetStatusByUrn assetStatusByUrn) {
        return Mutators.execute(session, Flux.just(
                assetStatusByUrnIdMutator.upsert(assetStatusByUrn),
                assetStatusByUrnMutator.upsert(assetStatusByUrn)
        )).doOnEach(log.reactiveErrorThrowable("error while saving asset status by urn and id",
                                               throwable -> new HashMap<String, Object>() {
                                                   {
                                                       put("id", assetStatusByUrn.getId());
                                                       put("assetUrn", assetStatusByUrn.getAssetUrn());
                                                   }
                                               }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist asset status error information
     *
     * @param assetStatusErrorByUrnId, the asset status error by urn id
     */
    @Trace(async = true)
    public Mono<Void> persistAssetStatusErrorByUrnId(AssetStatusErrorByUrnId assetStatusErrorByUrnId) {
        Flux<? extends Statement> iter = Mutators.upsert(assetStatusErrorByUrnIdMutator,
                                                         assetStatusErrorByUrnId);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving asset status error by urn id",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("id", assetStatusErrorByUrnId.getId());
                                                             put("assetUrn", assetStatusErrorByUrnId.getAssetUrn());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset status by urn and id
     * @param assetUrn the asset urn
     * @param assetId the asset id
     * @return mono of asset status bu urn
     */
    @Trace(async = true)
    public Mono<AssetStatusByUrn> fetchAssetStatusByUrnId(String assetUrn, UUID assetId) {
        return ResultSets.query(session, assetStatusByUrnIdMaterializer.findAssetStatus(assetUrn, assetId))
                .flatMapIterable(row -> row)
                .map(assetStatusByUrnIdMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching for asset status by urn id %s", assetUrn));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch asset status by urn
     *
     * @param assetUrn the asset urn
     * @return flux of asset status by urn
     */
    @Trace(async = true)
    public Flux<AssetStatusByUrn> fetchAssetStatusByUrn(String assetUrn) {
        return ResultSets.query(session, assetStatusByUrnMaterializer.findAssetStatusByUrn(assetUrn))
                .flatMapIterable(row -> row)
                .map(assetStatusByUrnMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching for asset status by urn %s", assetUrn));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch asset status error information
     * @param assetUrn the asset urn
     * @param assetId the asset id
     * @return mono of asset status error
     */
    @Trace(async = true)
    public Mono<AssetStatusErrorByUrnId> findAssetStatusError(String assetUrn, UUID assetId) {
        return ResultSets.query(session, assetStatusErrorByUrnIdMaterializer.findAssetStatusError(assetUrn, assetId))
                .flatMapIterable(row -> row)
                .map(assetStatusErrorByUrnIdMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching for asset status error by urn %s", assetUrn));
                    throw Exceptions.propagate(throwable);
                });
    }
}
